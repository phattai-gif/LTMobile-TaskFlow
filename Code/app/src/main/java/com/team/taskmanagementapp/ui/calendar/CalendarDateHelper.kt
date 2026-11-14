package com.team.taskmanagementapp.ui.calendar

import com.team.taskmanagementapp.ui.home.HomeTaskStatus
import com.team.taskmanagementapp.ui.home.HomeTaskUiModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Cùng một cách nhóm ngày cho dấu chấm và danh sách, tránh lệch dữ liệu. */
internal object CalendarDateHelper {
    private val timelineTime = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val cardTime = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

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

        // Thứ tự cột theo tiêu đề S, M, T, W, T, F, S trên màn hình Lịch.
        val firstOfMonth = month.atDay(1)
        val firstCell = firstOfMonth.minusDays((firstOfMonth.dayOfWeek.value % 7).toLong())
        val days = List(42) { index ->
            val date = firstCell.plusDays(index.toLong())
            val isCurrentMonth = YearMonth.from(date) == month
            CalendarDayUiModel(
                date = date,
                dayOfMonth = date.dayOfMonth,
                isCurrentMonth = isCurrentMonth,
                isSelected = date == selectedDate,
                isToday = isCurrentMonth && date == today,
                hasTasks = isCurrentMonth && date in tasksByDate
            )
        }

        val selectedTasks = tasksByDate[selectedDate].orEmpty().sortedWith(
            compareBy<HomeTaskUiModel> {
                val time = localTime(it.dueTime, zoneId)
                if (time == null) Int.MAX_VALUE else time.hour * 60 + time.minute
            }
                .thenBy { it.id }
        ).map { task ->
            val time = localTime(task.dueTime, zoneId)
            CalendarTaskUiModel(
                id = task.id,
                title = task.title,
                dueTimeFormatted = time?.format(cardTime) ?: "—",
                timeDisplay = time?.format(timelineTime) ?: "—",
                priorityLabel = task.priority.name.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() },
                priority = task.priority.name,
                isCompleted = task.isCompleted || task.status == HomeTaskStatus.COMPLETED
            )
        }
        return CalendarUiState(
            currentYearMonth = month,
            selectedDate = selectedDate,
            monthTitleFormatted = "tháng ${month.monthValue} ${month.year}",
            scheduleHeaderFormatted = "Schedule  Th${selectedDate.monthValue} ${selectedDate.dayOfMonth}",
            calendarDays = days,
            scheduledTasks = selectedTasks,
            isLoading = isLoading,
            hasError = hasError
        )
    }

    private fun localTime(timestamp: Long?, zoneId: ZoneId): LocalTime? =
        timestamp?.takeIf { it > 0L }?.let { Instant.ofEpochMilli(it).atZone(zoneId).toLocalTime() }
}
