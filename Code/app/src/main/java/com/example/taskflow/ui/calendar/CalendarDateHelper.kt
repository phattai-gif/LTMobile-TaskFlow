package com.example.taskflow.ui.calendar

import com.example.taskflow.ui.home.HomeTaskUiModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** Cùng một cách nhóm ngày cho dấu chấm và danh sách, tránh lệch dữ liệu. */
internal object CalendarDateHelper {
    fun buildUiState(
        selectedDate: LocalDate,
        tasks: List<HomeTaskUiModel>,
        zoneId: ZoneId,
        today: LocalDate,
        isLoading: Boolean = false,
        hasError: Boolean = false
    ): CalendarUiState {
        val month = YearMonth.from(selectedDate)
        val tasksByDate = tasks
            .filter { it.dueDate > 0L }
            .groupBy { task ->
                Instant.ofEpochMilli(task.dueDate).atZone(zoneId).toLocalDate()
            }

        // Thứ Hai là cột đầu tiên: MONDAY=1, ..., SUNDAY=7.
        val leadingEmptyCells = month.atDay(1).dayOfWeek.value - 1
        val days = List(42) { index ->
            val dayOfMonth = index - leadingEmptyCells + 1
            val date = if (dayOfMonth in 1..month.lengthOfMonth()) {
                month.atDay(dayOfMonth)
            } else null
            CalendarDayUiModel(
                date = date,
                taskCount = if (date == null) 0 else tasksByDate[date].orEmpty().size,
                isSelected = date == selectedDate,
                isToday = date == today
            )
        }

        val selectedTasks = tasksByDate[selectedDate].orEmpty().sortedWith(
            compareBy<HomeTaskUiModel> { minutesOfDay(it.dueTime, zoneId) }
                .thenBy { it.id }
        )
        return CalendarUiState(
            displayedMonth = month,
            selectedDate = selectedDate,
            days = days,
            tasksForSelectedDate = selectedTasks,
            isLoading = isLoading,
            hasError = hasError
        )
    }

    private fun minutesOfDay(timestamp: Long?, zoneId: ZoneId): Int {
        if (timestamp == null || timestamp <= 0L) return Int.MAX_VALUE
        val time = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalTime()
        return time.hour * 60 + time.minute
    }
}
