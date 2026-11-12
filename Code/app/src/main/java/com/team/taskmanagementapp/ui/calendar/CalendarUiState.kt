package com.team.taskmanagementapp.ui.calendar

import java.time.LocalDate
import java.time.YearMonth

data class CalendarDayUiModel(
    val date: LocalDate,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isSelected: Boolean,
    val isToday: Boolean,
    val hasTasks: Boolean
)

data class CalendarTaskUiModel(
    val id: Long,
    val title: String,
    val dueTimeFormatted: String, // e.g. "07:38 AM"
    val timeDisplay: String, // e.g. "07:38"
    val priorityLabel: String, // e.g. "Medium", "High", "Low"
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val isCompleted: Boolean = false
)

data class CalendarUiState(
    val currentYearMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val monthTitleFormatted: String = "",
    val scheduleHeaderFormatted: String = "",
    val calendarDays: List<CalendarDayUiModel> = emptyList(),
    val scheduledTasks: List<CalendarTaskUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false
)
