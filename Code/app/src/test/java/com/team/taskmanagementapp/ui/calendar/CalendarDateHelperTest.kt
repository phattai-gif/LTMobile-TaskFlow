package com.team.taskmanagementapp.ui.calendar

import com.team.taskmanagementapp.ui.home.HomeTaskStatus
import com.team.taskmanagementapp.ui.home.HomeTaskUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CalendarDateHelperTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val selected = LocalDate.of(2026, 11, 6)

    private fun task(id: Long, date: LocalDate): HomeTaskUiModel = HomeTaskUiModel(
        id = id,
        title = "Task $id",
        description = "",
        dueDate = date.atStartOfDay(zone).toInstant().toEpochMilli()
    )

    private fun build(tasks: List<HomeTaskUiModel>, date: LocalDate = selected): CalendarUiState =
        CalendarDateHelper.buildUiState(date, tasks, zone, selected)

    @Test
    fun november2026HasSevenColumnsAndCorrectWeekdayOffset() {
        val state = build(emptyList())
        assertEquals(42, state.calendarDays.size)
        assertTrue(state.calendarDays.chunked(7).all { it.size == 7 })
        assertEquals(LocalDate.of(2026, 11, 1), state.calendarDays[0].date)
        assertEquals(LocalDate.of(2026, 11, 30), state.calendarDays[29].date)
        assertTrue(state.calendarDays.drop(30).none { it.isCurrentMonth })
    }

    @Test
    fun leapFebruaryIncludes29Days() {
        val state = build(emptyList(), LocalDate.of(2028, 2, 10))
        assertTrue(state.calendarDays.take(2).none { it.isCurrentMonth })
        val dates = state.calendarDays.filter { it.isCurrentMonth }.map { it.date }
        assertEquals(29, dates.size)
        assertEquals(LocalDate.of(2028, 2, 29), dates.last())
    }

    @Test
    fun selectionListsOnlyTasksForThatLocalDate() {
        val state = build(listOf(
            task(1, selected.minusDays(1)),
            task(2, selected),
            task(3, selected.plusMonths(1))
        ))
        assertEquals(listOf(2L), state.scheduledTasks.map { it.id })
        assertTrue(state.calendarDays.single { it.date == selected }.hasTasks)
        assertFalse(state.calendarDays.single { it.date == selected.plusDays(1) }.hasTasks)
    }

    @Test
    fun timezoneIsUsedWhenAssigningDots() {
        val value = task(1, selected).copy(
            dueDate = Instant.parse("2026-11-05T18:30:00Z").toEpochMilli()
        )
        val state = build(listOf(value))
        assertEquals(listOf(1L), state.scheduledTasks.map { it.id })
        assertTrue(state.calendarDays.single { it.date == selected }.hasTasks)
        assertFalse(state.calendarDays.single { it.date == selected.minusDays(1) }.hasTasks)
    }

    @Test
    fun groupingUsesDueDateRatherThanDatePartOfDueTime() {
        val value = task(1, selected).copy(
            dueTime = selected.plusDays(1).atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
        )
        val state = build(listOf(value))
        assertEquals(1, state.scheduledTasks.size)
        assertTrue(state.calendarDays.single { it.date == selected }.hasTasks)
        assertFalse(state.calendarDays.single { it.date == selected.plusDays(1) }.hasTasks)
    }

    @Test
    fun invalidOrMissingDatesAreExcluded() {
        val state = build(listOf(
            task(1, selected).copy(dueDate = 0L),
            task(2, selected).copy(dueDate = -1L)
        ))
        assertTrue(state.scheduledTasks.isEmpty())
        assertTrue(state.calendarDays.none { it.hasTasks })
    }

    @Test
    fun duplicateTitlesRemainSeparateTasks() {
        val first = task(1, selected).copy(title = "Trùng tiêu đề")
        val second = task(2, selected).copy(title = "Trùng tiêu đề")
        val state = build(listOf(first, second))
        assertEquals(2, state.scheduledTasks.size)
        assertEquals(listOf(1L, 2L), state.scheduledTasks.map { it.id })
        assertTrue(state.calendarDays.single { it.date == selected }.hasTasks)
    }

    @Test
    fun completedTasksStillHaveCalendarDots() {
        val completed = task(1, selected).copy(status = HomeTaskStatus.COMPLETED)
        val state = build(listOf(completed))
        assertTrue(state.scheduledTasks.single().isCompleted)
        assertTrue(state.calendarDays.single { it.date == selected }.hasTasks)
    }

    @Test
    fun listSortsByTimeOfDayThenIdAndPutsMissingTimesLast() {
        val later = task(1, selected).copy(
            dueTime = LocalDate.of(2024, 1, 1).atTime(10, 15)
                .atZone(zone).toInstant().toEpochMilli()
        )
        val earlier = task(2, selected).copy(
            dueTime = selected.atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
        )
        val sameTime = task(3, selected).copy(dueTime = earlier.dueTime)
        val noTime = task(4, selected)
        val zeroTime = task(5, selected).copy(dueTime = 0L)
        val state = build(listOf(noTime, sameTime, later, zeroTime, earlier))
        assertEquals(listOf(2L, 3L, 1L, 4L, 5L), state.scheduledTasks.map { it.id })
        assertEquals(listOf("09:00", "09:00", "10:15", "—", "—"), state.scheduledTasks.map { it.timeDisplay })
    }

    @Test
    fun selectedAndTodayMarkersAreIndependent() {
        val state = build(emptyList(), selected.plusDays(2))
        assertTrue(state.calendarDays.single { it.date == selected }.isToday)
        assertFalse(state.calendarDays.single { it.date == selected }.isSelected)
        assertTrue(state.calendarDays.single { it.date == selected.plusDays(2) }.isSelected)
        assertFalse(state.calendarDays.single { it.date == selected.plusDays(2) }.isToday)
    }
}
