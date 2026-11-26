package com.team.taskmanagementapp.ai

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Service providing Gemini AI capabilities powered by Firebase AI Logic.
 */
object FirebaseAiService {
    private const val TAG = "FirebaseAiService"
    private const val MODEL_NAME = "gemini-4-hydrogen"
    private const val FALLBACK_MODEL_NAME = "gemini-1.5-flash"

    private val generativeModel: GenerativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(MODEL_NAME)
    }

    private val fallbackModel: GenerativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(FALLBACK_MODEL_NAME)
    }

    /**
     * Generates an actionable response using Gemini and executes any requested task actions directly.
     */
    suspend fun generateActionableResponse(
        context: Context,
        prompt: String
    ): AiParsedActionResult = withContext(Dispatchers.IO) {
        val nowCal = Calendar.getInstance()
        val viDateFmt = SimpleDateFormat("EEEE, dd/MM/yyyy HH:mm", Locale("vi", "VN"))
        val currentDateTimeStr = viDateFmt.format(nowCal.time)
        val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(nowCal.time)

        val tasksContext = buildTasksContext(context)

        val systemInstruction = """
            You are TaskFlow AI, the proactive personal assistant directly integrated inside the TaskFlow Android app.
            
            CURRENT TIME & CALENDAR:
            - Current Date & Time: $currentDateTimeStr (Today: $todayIso).
            - Today's day of week is: ${SimpleDateFormat("EEEE", Locale("vi", "VN")).format(nowCal.time)}.
            
            DIRECT EXECUTION CAPABILITIES:
            You HAVE direct execution authority in TaskFlow. You CAN create tasks, complete tasks, delete tasks, and inspect user tasks directly.
            NEVER tell the user that you are not connected to TaskFlow storage or cannot save tasks, because the app automatically executes your action blocks into the local database!
            
            RULES FOR ACTIONS:
            1. CREATE A TASK:
               When the user asks to create or schedule a task (e.g., "Tạo cho mình 1 task...", "Thêm việc...", "Nhắc mình...", "Lên lịch..."):
               - Calculate the exact target date (in YYYY-MM-DD format) based on current date $todayIso and user's intent (e.g. "thứ 6", "ngày mai", "tuần sau").
               - Determine target time (in HH:mm 24-hour format, e.g. "12:00", "09:00", "15:30").
               - Determine priority: "HIGH", "MEDIUM", or "LOW".
               - You MUST output an action block formatted exactly like this:
               ```action
               {
                 "action": "CREATE_TASK",
                 "title": "<Title of the task>",
                 "description": "<Detail or description>",
                 "date": "YYYY-MM-DD",
                 "time": "HH:mm",
                 "priority": "HIGH"
               }
               ```
               - Then write a brief, polite confirmation message in the user's language confirming that you have directly created and scheduled the task.
               
            2. COMPLETE A TASK:
               When the user says they finished a task or asks to complete one (e.g., "Đánh dấu hoàn thành...", "Xong task X rồi"):
               ```action
               {
                 "action": "COMPLETE_TASK",
                 "title": "<Keyword or title of task>"
               }
               ```
               
            3. DELETE A TASK:
               When the user asks to delete or remove a task:
               ```action
               {
                 "action": "DELETE_TASK",
                 "title": "<Keyword or title of task>"
               }
               ```
               
            4. GENERAL QUERIES & LISTING TASKS:
               When the user asks about their tasks or schedule, reference the User Tasks Context below to provide a helpful answer.
               
            Keep your conversational message concise, friendly, and helpful. Always respond in the user's language (Vietnamese or English).
        """.trimIndent()

        val fullPrompt = buildString {
            append(systemInstruction)
            append("\n\n")
            if (tasksContext.isNotBlank()) {
                append("--- USER'S ACTIVE TASKS CONTEXT ---\n")
                append(tasksContext)
                append("\n------------------------------------\n\n")
            }
            append("User: ")
            append(prompt)
        }

        try {
            val response = try {
                generativeModel.generateContent(fullPrompt)
            } catch (primaryError: Exception) {
                Log.w(TAG, "Primary model $MODEL_NAME error, trying fallback $FALLBACK_MODEL_NAME", primaryError)
                fallbackModel.generateContent(fullPrompt)
            }
            val rawOutput = response.text?.trim() ?: ""
            if (rawOutput.isBlank()) {
                return@withContext AiParsedActionResult(cleanText = "Không nhận được phản hồi từ AI, vui lòng thử lại.")
            }

            AiActionExecutor.parseAndExecute(context, rawOutput)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating AI response via Firebase AI Logic", e)
            throw e
        }
    }

    private suspend fun buildTasksContext(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val dao = AppDatabase.getInstance(context).taskDao()
            val activeTasks = dao.getActiveTasksSync()
            if (activeTasks.isEmpty()) return@withContext "User has no active tasks currently."

            val dateFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            buildString {
                append("Active tasks count: ${activeTasks.size}\n")
                activeTasks.take(15).forEach { task ->
                    val combinedDue = DateTimeUtils.getCombinedDueTimestamp(task.dueDate, task.dueTime)
                    val dueStr = if (combinedDue > 0L) dateFmt.format(Date(combinedDue)) else "No deadline"
                    append("- [ID ${task.id}] \"${task.title}\" | Due: $dueStr | Priority: ${task.priority.name} | Status: ${task.status.name}\n")
                }
            }
        } catch (e: Exception) {
            ""
        }
    }
}
