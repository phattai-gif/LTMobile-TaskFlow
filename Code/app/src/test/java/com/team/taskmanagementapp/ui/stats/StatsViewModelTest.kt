package com.team.taskmanagementapp.ui.stats

import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.data.model.stats.StatsTimeFilter
import com.team.taskmanagementapp.data.model.stats.TaskFocusStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StatsViewModelTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val monday = LocalDate.of(2026, 11, 9)
    private val today = monday.plusDays(5)

    @Test
    fun recordedCompletionDatesDriveWeekdayChartAndSelectedPeriodTotals() {
        val tasks = listOf(
            task(1, dueDate = monday.minusWeeks(1), completedOn = monday.plusDays(1), priority = Priority.HIGH),
            task(2, dueDate = monday, priority = Priority.URGENT),
            task(3, dueDate = monday, completedOn = monday.minusDays(1), priority = Priority.LOW),
            task(4, dueDate = monday, status = TaskStatus.COMPLETED)
        )

        val week = stats(tasks)
        assertEquals(listOf(0, 1, 0, 0, 0, 0, 0), week.weekdayCompletions.map { it.count })
        assertEquals(1, week.completedCount)
        assertEquals(1, week.pendingCount)
        assertEquals(50, week.completionRatePercent)
        assertEquals(PriorityDistribution(urgentCount = 1, highCount = 1, totalCount = 2), week.priorityDistribution)
        assertTrue(week.hasUnknownCompletionDates)

        val allTime = stats(tasks, filter = StatsTimeFilter.ALL_TIME)
        assertEquals(3, allTime.completedCount)
        assertEquals(2, allTime.weekdayCompletions.sumOf { it.count })
        assertEquals(75, allTime.completionRatePercent)
    }

    @Test
    fun chartScaleAccommodatesMoreThanTwelveCompletions() {
        val tasks = (1..16).map { task(it, completedOn = monday) }
        val state = stats(tasks)

        assertEquals(16, state.weekdayCompletions.first().count)
        assertEquals(18, state.maxWeekdayCount)
        assertEquals(7, state.weekdayCompletions.size)
    }

    @Test
    fun emptyPeriodUsesLoadedStateAndKeepsFocusOnlyDataVisible() {
        val pending = task(1, dueDate = monday.minusWeeks(1))

        val empty = stats(listOf(pending))
        assertFalse(empty.isLoading)
        assertTrue(empty.isEmpty)
        assertEquals(0, empty.completionRatePercent)

        val focusOnly = stats(listOf(pending), focusByTask = listOf(TaskFocusStats(1, 25, 1)))
        assertFalse(focusOnly.isEmpty)
        assertEquals(0, focusOnly.priorityDistribution.totalCount)
        assertEquals(25, focusOnly.pomodoroStats.periodMinutes)
    }

    @Test
    fun pomodoroUsesSessionMinutesAndNamesTopTasks() {
        val tasks = (1..4).map { task(it).copy(totalFocusTimeMinutes = 500) }
        val state = stats(
            tasks,
            todayMinutes = 25,
            weekMinutes = 50,
            focusByTask = listOf(
                TaskFocusStats(1, 25, 1),
                TaskFocusStats(2, 50, 2),
                TaskFocusStats(3, 75, 3),
                TaskFocusStats(4, 5, 1)
            )
        )

        assertEquals(25, state.pomodoroStats.todayMinutes)
        assertEquals(50, state.pomodoroStats.thisWeekMinutes)
        assertEquals(155, state.pomodoroStats.periodMinutes)
        assertEquals(7, state.pomodoroStats.periodSessionCount)
        assertEquals(listOf("Task 3", "Task 2", "Task 1"), state.pomodoroStats.topTasks.map { it.taskTitle })
    }

    @Test
    fun dateFiltersUseLocalMidnightAndExcludeNextPeriod() {
        val dstZone = ZoneId.of("America/New_York")
        val sunday = LocalDate.of(2026, 3, 8)
        val range = statsPeriod(StatsTimeFilter.THIS_WEEK, sunday, dstZone)
        val start = LocalDate.of(2026, 3, 2).atStartOfDay(dstZone).toInstant().toEpochMilli()
        val nextMonday = sunday.plusDays(1).atStartOfDay(dstZone).toInstant().toEpochMilli()

        assertEquals(start, range.first)
        assertEquals(nextMonday - 1, range.last)
        assertFalse(nextMonday in range)
        assertEquals(
            range.first - 1,
            statsPeriod(StatsTimeFilter.LAST_WEEK, sunday, dstZone).last
        )
        assertEquals(
            today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli(),
            statsPeriod(StatsTimeFilter.THIS_MONTH, today, zone).first
        )
    }

    private fun stats(
        tasks: List<Task>,
        filter: StatsTimeFilter = StatsTimeFilter.THIS_WEEK,
        todayMinutes: Int = 0,
        weekMinutes: Int = 0,
        focusByTask: List<TaskFocusStats> = emptyList()
    ): StatsUiState = calculateStats(
        tasks, filter, statsPeriod(filter, today, zone), zone, todayMinutes, weekMinutes, focusByTask
    )

    private fun task(
        id: Int,
        dueDate: LocalDate = monday,
        completedOn: LocalDate? = null,
        priority: Priority = Priority.MEDIUM,
        status: TaskStatus = TaskStatus.TODO
    ) = Task(
        id = id,
        title = "Task $id",
        description = "",
        dueDate = dueDate.atStartOfDay(zone).toInstant().toEpochMilli(),
        dueTime = 0L,
        priority = priority,
        status = status,
        isCompleted = completedOn != null,
        completedAt = completedOn?.atTime(12, 0)?.atZone(zone)?.toInstant()?.toEpochMilli()
    )
}
