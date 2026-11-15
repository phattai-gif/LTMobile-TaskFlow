package com.team.taskmanagementapp.util

import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class TaskReminderTimingTest {
    @Test
    fun reminderUsesSelectedDateAndTime() {
        val task = task().copy(dueTime = time(day = 11, hour = 14, minute = 20))

        assertEquals(time(day = 12, hour = 14, minute = 10), TaskReminderTiming.reminderAt(task))
    }

    @Test
    fun reminderIsSkippedForDisabledCompletedOrPausedTasks() {
        val task = task()

        assertNull(TaskReminderTiming.reminderAt(task.copy(reminderMinutes = 0)))
        assertNull(TaskReminderTiming.reminderAt(task.copy(isCompleted = true)))
        assertNull(TaskReminderTiming.reminderAt(task.copy(status = TaskStatus.COMPLETED)))
        assertNull(TaskReminderTiming.reminderAt(task.copy(isPaused = true)))
        assertNull(TaskReminderTiming.reminderAt(task.copy(dueDate = 0)))
        assertNull(TaskReminderTiming.reminderAt(task.copy(dueTime = 0)))
    }

    @Test
    fun taskStaysActiveUntilEndOfDueMinute() {
        val task = task().copy(status = TaskStatus.IN_PROGRESS)
        val minuteStart = time(day = 12, hour = 14, minute = 20)

        assertEquals(
            TaskStatus.IN_PROGRESS,
            TaskReminderTiming.statusAfterClockChange(task, minuteStart + 59_999L)
        )
        assertEquals(
            TaskStatus.OVERDUE,
            TaskReminderTiming.statusAfterClockChange(task, minuteStart + 60_000L)
        )
    }

    @Test
    fun futureTaskReturnsToTodoAfterClockMovesBack() {
        assertEquals(
            TaskStatus.TODO,
            TaskReminderTiming.statusAfterClockChange(
                task().copy(status = TaskStatus.OVERDUE),
                time(day = 12, hour = 13, minute = 0)
            )
        )
    }

    @Test
    fun recoveryPreservesCompletedTasksAndTasksWithoutDeadline() {
        val now = time(day = 13, hour = 9, minute = 0)

        assertEquals(
            TaskStatus.COMPLETED,
            TaskReminderTiming.statusAfterClockChange(task().copy(status = TaskStatus.COMPLETED), now)
        )
        assertEquals(
            TaskStatus.IN_PROGRESS,
            TaskReminderTiming.statusAfterClockChange(
                task().copy(dueDate = 0, status = TaskStatus.IN_PROGRESS),
                now
            )
        )
    }

    private fun task() = Task(
        title = "Test reminder",
        description = "",
        dueDate = time(day = 12, hour = 0, minute = 0),
        dueTime = time(day = 12, hour = 14, minute = 20),
        priority = Priority.MEDIUM,
        reminderMinutes = 10
    )

    private fun time(day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.NOVEMBER, day, hour, minute)
        }.timeInMillis
}
