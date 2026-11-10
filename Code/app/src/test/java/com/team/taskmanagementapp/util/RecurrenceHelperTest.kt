package com.team.taskmanagementapp.util

import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.RecurrenceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class RecurrenceHelperTest {

    private fun millisFromDate(year: Int, month: Int, day: Int): Long {
        return LocalDate.of(year, month, day)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    @Test
    fun calculateNextDueDate_daily_addsOneDay() {
        val startMillis = millisFromDate(2026, 8, 26)
        val nextMillis = RecurrenceHelper.calculateNextDueDate(startMillis, RecurrenceType.DAILY, 1)

        val expectedMillis = millisFromDate(2026, 8, 27)
        assertEquals(expectedMillis, nextMillis)
    }

    @Test
    fun calculateNextDueDate_dailyWithInterval_addsMultipleDays() {
        val startMillis = millisFromDate(2026, 8, 26)
        val nextMillis = RecurrenceHelper.calculateNextDueDate(startMillis, RecurrenceType.DAILY, 3)

        val expectedMillis = millisFromDate(2026, 8, 29)
        assertEquals(expectedMillis, nextMillis)
    }

    @Test
    fun calculateNextDueDate_weekly_addsSevenDays() {
        val startMillis = millisFromDate(2026, 8, 26)
        val nextMillis = RecurrenceHelper.calculateNextDueDate(startMillis, RecurrenceType.WEEKLY, 1)

        val expectedMillis = millisFromDate(2026, 9, 2)
        assertEquals(expectedMillis, nextMillis)
    }

    @Test
    fun calculateNextDueDate_weeklyWithInterval_addsMultipleWeeks() {
        val startMillis = millisFromDate(2026, 8, 26)
        val nextMillis = RecurrenceHelper.calculateNextDueDate(startMillis, RecurrenceType.WEEKLY, 2)

        val expectedMillis = millisFromDate(2026, 9, 9)
        assertEquals(expectedMillis, nextMillis)
    }

    @Test
    fun calculateNextDueDate_monthly_edgeCaseJan31NonLeapYear() {
        // Jan 31, 2026 -> +1 month should land on Feb 28, 2026 (non-leap year)
        val startMillis = millisFromDate(2026, 1, 31)
        val nextMillis = RecurrenceHelper.calculateNextDueDate(startMillis, RecurrenceType.MONTHLY, 1)

        val expectedMillis = millisFromDate(2026, 2, 28)
        assertEquals(expectedMillis, nextMillis)
    }

    @Test
    fun calculateNextDueDate_monthly_edgeCaseJan31LeapYear() {
        // Jan 31, 2024 -> +1 month should land on Feb 29, 2024 (leap year)
        val startMillis = millisFromDate(2024, 1, 31)
        val nextMillis = RecurrenceHelper.calculateNextDueDate(startMillis, RecurrenceType.MONTHLY, 1)

        val expectedMillis = millisFromDate(2024, 2, 29)
        assertEquals(expectedMillis, nextMillis)
    }

    @Test
    fun calculateNextDueDate_monthly_edgeCaseAug31() {
        // Aug 31, 2026 -> +1 month should land on Sep 30, 2026
        val startMillis = millisFromDate(2026, 8, 31)
        val nextMillis = RecurrenceHelper.calculateNextDueDate(startMillis, RecurrenceType.MONTHLY, 1)

        val expectedMillis = millisFromDate(2026, 9, 30)
        assertEquals(expectedMillis, nextMillis)
    }

    @Test
    fun calculateNextDueDate_yearly_addsOneYear() {
        val startMillis = millisFromDate(2026, 8, 26)
        val nextMillis = RecurrenceHelper.calculateNextDueDate(startMillis, RecurrenceType.YEARLY, 1)

        val expectedMillis = millisFromDate(2027, 8, 26)
        assertEquals(expectedMillis, nextMillis)
    }

    @Test
    fun calculateNextDueDate_none_returnsOriginalTimestamp() {
        val startMillis = millisFromDate(2026, 8, 26)
        val nextMillis = RecurrenceHelper.calculateNextDueDate(startMillis, RecurrenceType.NONE, 1)

        assertEquals(startMillis, nextMillis)
    }

    @Test
    fun calculateEndDateFromOccurrences_calculatesCorrectLimits() {
        val startMillis = millisFromDate(2026, 8, 26)

        // 3 days
        val dailyEnd = RecurrenceHelper.calculateEndDateFromOccurrences(startMillis, RecurrenceType.DAILY, 3)
        assertEquals(millisFromDate(2026, 8, 29), dailyEnd)

        // 2 weeks
        val weeklyEnd = RecurrenceHelper.calculateEndDateFromOccurrences(startMillis, RecurrenceType.WEEKLY, 2)
        assertEquals(millisFromDate(2026, 9, 9), weeklyEnd)

        // 2 years
        val yearlyEnd = RecurrenceHelper.calculateEndDateFromOccurrences(startMillis, RecurrenceType.YEARLY, 2)
        assertEquals(millisFromDate(2028, 8, 26), yearlyEnd)

        // Count 0 returns 0L
        assertEquals(0L, RecurrenceHelper.calculateEndDateFromOccurrences(startMillis, RecurrenceType.DAILY, 0))
    }

    @Test
    fun isRecurrenceEnded_respectsMaxCountAndEndDateAndPaused() {
        val startMillis = millisFromDate(2026, 8, 26)
        val task = Task(
            id = 1,
            title = "Test Task",
            description = "Description",
            dueDate = startMillis,
            dueTime = 0L,
            priority = Priority.MEDIUM,
            isRecurring = true,
            recurrenceType = RecurrenceType.DAILY,
            repeatLimitCount = 3,
            currentOccurrence = 3
        )
        val nextMillis = millisFromDate(2026, 8, 27)

        // Reached limit
        assertTrue(RecurrenceHelper.isRecurrenceEnded(task, nextMillis))

        // Not yet reached limit
        val notEndedTask = task.copy(currentOccurrence = 2)
        assertFalse(RecurrenceHelper.isRecurrenceEnded(notEndedTask, nextMillis))

        // Paused task
        val pausedTask = notEndedTask.copy(isPaused = true)
        assertTrue(RecurrenceHelper.isRecurrenceEnded(pausedTask, nextMillis))

        // With repeatEndDate
        val taskWithEndDate = task.copy(
            repeatLimitCount = 0,
            repeatEndDate = millisFromDate(2026, 8, 28)
        )
        assertFalse(RecurrenceHelper.isRecurrenceEnded(taskWithEndDate, millisFromDate(2026, 8, 27)))
        assertTrue(RecurrenceHelper.isRecurrenceEnded(taskWithEndDate, millisFromDate(2026, 8, 29)))
    }
}
