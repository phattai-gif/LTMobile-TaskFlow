package com.team.taskmanagementapp.ai

import android.content.Context
import android.util.Log
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.util.AlarmScheduler
import com.team.taskmanagementapp.util.DateTimeUtils
import com.team.taskmanagementapp.util.TaskReminderTiming
import com.team.taskmanagementapp.widget.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AiParsedActionResult(
    val cleanText: String,
    val executedTask: Task? = null,
    val actionType: String? = null // "CREATED", "COMPLETED", "DELETED"
)

object AiActionExecutor {
    private const val TAG = "AiActionExecutor"

    private val ACTION_REGEX = Regex("```(?:action|json:action)?\\s*(\\{[\\s\\S]*?\\})\\s*```", RegexOption.IGNORE_CASE)

    /**
     * Parses and executes any action embedded in the AI response.
     */
    suspend fun parseAndExecute(context: Context, rawResponse: String): AiParsedActionResult = withContext(Dispatchers.IO) {
        val match = ACTION_REGEX.find(rawResponse)
        if (match == null) {
            return@withContext AiParsedActionResult(cleanText = rawResponse.trim())
        }

        val jsonStr = match.groupValues[1]
        val cleanText = rawResponse.replace(match.value, "").trim()

        try {
            val json = JSONObject(jsonStr)
            val action = json.optString("action", "").uppercase()
            val database = AppDatabase.getInstance(context)
            val taskDao = database.taskDao()

            when (action) {
                "CREATE_TASK" -> {
                    val title = json.optString("title", "").trim()
                    val description = json.optString("description", "").trim()
                    val dateStr = json.optString("date", "").trim() // YYYY-MM-DD
                    val timeStr = json.optString("time", "").trim() // HH:mm
                    val priorityStr = json.optString("priority", "MEDIUM").uppercase()

                    val (dueDateMillis, dueTimeMillis) = parseDateTime(dateStr, timeStr)
                    val priority = try {
                        Priority.valueOf(priorityStr)
                    } catch (_: Exception) {
                        Priority.MEDIUM
                    }

                    val now = System.currentTimeMillis()
                    val newTask = Task(
                        title = title.ifBlank { "Task mới" },
                        description = description,
                        dueDate = dueDateMillis,
                        dueTime = dueTimeMillis,
                        priority = priority,
                        status = TaskStatus.TODO,
                        isCompleted = false,
                        createdAt = now,
                        updatedAt = now
                    )

                    val newId = taskDao.insertTask(newTask)
                    val createdTask = newTask.copy(id = newId.toInt())

                    // Schedule alarm if applicable
                    val reminderAt = TaskReminderTiming.reminderAt(createdTask)
                    if (reminderAt != null && reminderAt > System.currentTimeMillis()) {
                        AlarmScheduler.schedule(context, createdTask.id, reminderAt)
                    }

                    // Update widgets
                    try {
                        WidgetUpdater.updateAllWidgets(context)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to update widgets after AI task creation", e)
                    }

                    val responseText = if (cleanText.isNotBlank()) {
                        cleanText
                    } else {
                        "Mình đã trực tiếp tạo và lưu task vào danh sách công việc của bạn:"
                    }

                    return@withContext AiParsedActionResult(
                        cleanText = responseText,
                        executedTask = createdTask,
                        actionType = "CREATED"
                    )
                }

                "COMPLETE_TASK" -> {
                    val titleQuery = json.optString("title", "").trim().lowercase()
                    val activeTasks = taskDao.getActiveTasksSync()
                    val targetTask = activeTasks.firstOrNull {
                        it.title.lowercase().contains(titleQuery)
                    }

                    if (targetTask != null) {
                        val now = System.currentTimeMillis()
                        val updated = targetTask.copy(
                            isCompleted = true,
                            status = TaskStatus.COMPLETED,
                            completedAt = now,
                            updatedAt = now
                        )
                        taskDao.updateTask(updated)
                        try {
                            WidgetUpdater.updateAllWidgets(context)
                        } catch (_: Exception) {}

                        val responseText = if (cleanText.isNotBlank()) {
                            cleanText
                        } else {
                            "Đã đánh dấu hoàn thành task \"${targetTask.title}\"!"
                        }

                        return@withContext AiParsedActionResult(
                            cleanText = responseText,
                            executedTask = updated,
                            actionType = "COMPLETED"
                        )
                    }
                }

                "DELETE_TASK" -> {
                    val titleQuery = json.optString("title", "").trim().lowercase()
                    val allTasks = taskDao.getAllTasksSync()
                    val targetTask = allTasks.firstOrNull {
                        it.title.lowercase().contains(titleQuery)
                    }

                    if (targetTask != null) {
                        AlarmScheduler.cancel(context, targetTask.id)
                        taskDao.deleteTask(targetTask)
                        try {
                            WidgetUpdater.updateAllWidgets(context)
                        } catch (_: Exception) {}

                        val responseText = if (cleanText.isNotBlank()) {
                            cleanText
                        } else {
                            "Đã xóa task \"${targetTask.title}\" khỏi danh sách."
                        }

                        return@withContext AiParsedActionResult(
                            cleanText = responseText,
                            executedTask = targetTask,
                            actionType = "DELETED"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing AI action: $jsonStr", e)
        }

        AiParsedActionResult(cleanText = cleanText.ifBlank { rawResponse.trim() })
    }

    private fun parseDateTime(dateStr: String, timeStr: String): Pair<Long, Long> {
        val calendar = Calendar.getInstance()

        // Parse date (YYYY-MM-DD)
        if (dateStr.isNotBlank()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val parsedDate = sdf.parse(dateStr)
                if (parsedDate != null) {
                    val dateCal = Calendar.getInstance().apply { time = parsedDate }
                    calendar.set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
                    calendar.set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
                    calendar.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))
                }
            } catch (_: Exception) {}
        }

        // Parse time (HH:mm)
        if (timeStr.isNotBlank()) {
            try {
                val timeParts = timeStr.split(":")
                if (timeParts.size >= 2) {
                    val hour = timeParts[0].trim().toInt()
                    val minute = timeParts[1].trim().toInt()
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }
            } catch (_: Exception) {}
        } else {
            // Default to 12:00 if not specified
            calendar.set(Calendar.HOUR_OF_DAY, 12)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }

        val dueDateMillis = DateTimeUtils.getStartOfDay(calendar.timeInMillis)
        val dueTimeMillis = calendar.timeInMillis
        return Pair(dueDateMillis, dueTimeMillis)
    }
}
