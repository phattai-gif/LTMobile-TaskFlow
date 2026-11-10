package com.team.taskmanagementapp.ui.calendar

import com.team.taskmanagementapp.ui.home.HomeTaskUiModel
import java.time.LocalDate
import java.time.YearMonth

data class CalendarDayUiModel(
    val date: LocalDate?,
    val taskCount: Int = 0,
    val isSelected: Boolean = false,
    val isToday: Boolean = false
) {
    val hasTasks: Boolean get() = taskCount > 0
}

data class CalendarUiState(
    val displayedMonth: YearMonth,
    val selectedDate: LocalDate,
    val days: List<CalendarDayUiModel>,
    val tasksForSelectedDate: List<HomeTaskUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false
)
