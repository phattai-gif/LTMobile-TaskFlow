package com.team.taskmanagementapp.util

import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.TaskStatus

internal object TaskReminderTiming {
    private const val MILLIS_PER_MINUTE = 60_000L

    fun deadline(task: Task): Long =
        DateTimeUtils.getCombinedDueTimestamp(task.dueDate, task.dueTime)

    fun reminderAt(task: Task): Long? {
        if (task.isCompleted || task.status == TaskStatus.COMPLETED || task.isPaused ||
            task.reminderMinutes <= 0 || task.dueDate <= 0L || task.dueTime <= 0L
        ) {
            return null
        }

        // The deadline includes the whole selected minute. Start the reminder at minute zero.
        val deadlineMinute = deadline(task) - (MILLIS_PER_MINUTE - 1)
        return deadlineMinute - task.reminderMinutes * MILLIS_PER_MINUTE
    }

    fun statusAfterClockChange(task: Task, now: Long): TaskStatus {
        if (task.isCompleted || task.status == TaskStatus.COMPLETED) return task.status

        val deadline = deadline(task)
        return when {
            deadline > 0L && deadline < now -> TaskStatus.OVERDUE
            deadline > now && task.status == TaskStatus.OVERDUE -> TaskStatus.TODO
            else -> task.status
        }
    }
}
