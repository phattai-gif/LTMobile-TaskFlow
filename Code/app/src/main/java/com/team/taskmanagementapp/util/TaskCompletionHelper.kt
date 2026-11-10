package com.team.taskmanagementapp.util

import android.content.Context
import com.team.taskmanagementapp.data.local.dao.TaskDao
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.RecurrenceType
import com.team.taskmanagementapp.data.model.enums.TaskStatus

/**
 * Helper object handling task completion logic and recurring task side-effects:
 * - When completing a recurring task: checks limits (repeatLimitCount, repeatEndDate, isPaused),
 *   calculates next due date with RecurrenceHelper, and generates the next recurring instance.
 * - When reopening/uncompleting: cleans up any auto-generated future instances.
 */
object TaskCompletionHelper {

    /**
     * Handles recurrence side-effects upon marking a task completed or uncompleted.
     * @return Next generated recurring Task instance if created, or null.
     */
    suspend fun handleRecurrenceSideEffects(
        dao: TaskDao,
        originalTask: Task,
        targetCompleted: Boolean,
        now: Long = System.currentTimeMillis()
    ): Task? {
        val isRecurring = originalTask.isRecurring || originalTask.recurrenceType != RecurrenceType.NONE
        if (!isRecurring) return null

        val nextDueDate = RecurrenceHelper.calculateNextDueDate(
            originalTask.dueDate,
            originalTask.recurrenceType,
            originalTask.recurrenceInterval
        )
        val nextDueTime = if (originalTask.dueTime > 0L) {
            RecurrenceHelper.calculateNextDueDate(
                originalTask.dueTime,
                originalTask.recurrenceType,
                originalTask.recurrenceInterval
            )
        } else {
            originalTask.dueTime
        }

        if (targetCompleted) {
            val isEnded = RecurrenceHelper.isRecurrenceEnded(originalTask, nextDueDate)
            if (!isEnded && !originalTask.isPaused) {
                val existingFutureTasks = dao.getFutureRecurringTasksSync(
                    originalTask.title,
                    originalTask.recurrenceType,
                    nextDueDate
                )

                if (existingFutureTasks.isEmpty()) {
                    val nextInstance = originalTask.copy(
                        id = 0,
                        isCompleted = false,
                        completedAt = null,
                        status = TaskStatus.TODO,
                        isRecurring = true,
                        recurrenceType = originalTask.recurrenceType,
                        recurrenceInterval = originalTask.recurrenceInterval,
                        dueDate = nextDueDate,
                        dueTime = nextDueTime,
                        repeatEndDate = originalTask.repeatEndDate,
                        repeatLimitCount = originalTask.repeatLimitCount,
                        currentOccurrence = originalTask.currentOccurrence + 1,
                        isPaused = originalTask.isPaused,
                        createdAt = now,
                        updatedAt = now
                    )
                    val insertedId = dao.insertTask(nextInstance)
                    return nextInstance.copy(id = insertedId.toInt())
                }
            }
            return null
        } else {
            // Reopening task: delete future uncompleted recurring instances generated from this task
            dao.deleteFutureRecurringTasks(
                originalTask.title,
                originalTask.recurrenceType,
                nextDueDate
            )
            return null
        }
    }

    /**
     * Toggles task completion by id (useful for external receivers, widgets, shortcuts).
     */
    suspend fun toggleById(context: Context, taskId: Int): Boolean {
        val appContext = context.applicationContext
        val dao = AppDatabase.getInstance(appContext).taskDao()
        val task = dao.getTaskById(taskId.toLong()) ?: return false
        val targetCompleted = !task.isCompleted
        val now = System.currentTimeMillis()

        val updatedTask = task.copy(
            isCompleted = targetCompleted,
            completedAt = if (targetCompleted) now else null,
            status = if (targetCompleted) TaskStatus.COMPLETED else TaskStatus.TODO,
            updatedAt = now
        )
        dao.updateTask(updatedTask)
        handleRecurrenceSideEffects(dao, task, targetCompleted, now)
        return true
    }
}
