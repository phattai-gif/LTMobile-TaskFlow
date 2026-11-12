package com.team.taskmanagementapp.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class CalendarViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val currentYearMonth = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<CalendarUiState> = combine(
        currentYearMonth,
        selectedDate,
        repository.getAllTasks()
    ) { yearMonth, selected, tasks ->
        buildCalendarUiState(yearMonth, selected, tasks)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(
            monthTitleFormatted = "tháng ${YearMonth.now().monthValue} ${YearMonth.now().year}",
            scheduleHeaderFormatted = "Schedule  Th${LocalDate.now().monthValue} ${LocalDate.now().dayOfMonth}"
        )
    )

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
        if (YearMonth.from(date) != currentYearMonth.value) {
            currentYearMonth.value = YearMonth.from(date)
        }
    }

    fun previousMonth() {
        currentYearMonth.value = currentYearMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        currentYearMonth.value = currentYearMonth.value.plusMonths(1)
    }

    private fun buildCalendarUiState(
        yearMonth: YearMonth,
        selected: LocalDate,
        tasks: List<Task>
    ): CalendarUiState {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()

        // Group tasks by LocalDate
        val tasksByDate = tasks.groupBy { task ->
            if (task.dueDate > 0L) {
                Instant.ofEpochMilli(task.dueDate).atZone(zone).toLocalDate()
            } else {
                null
            }
        }

        val datesWithTasks = tasksByDate.keys.filterNotNull().toSet()

        // Month title formatted: "tháng 10 2026"
        val monthTitle = "tháng ${yearMonth.monthValue} ${yearMonth.year}"

        // Schedule header: "Schedule  Th10 6"
        val scheduleHeader = "Schedule  Th${selected.monthValue} ${selected.dayOfMonth}"

        // Calendar grid: Sunday = 0, Monday = 1, ..., Saturday = 6
        val firstOfMonth = yearMonth.atDay(1)
        val firstDayOfWeek = firstOfMonth.dayOfWeek.value % 7 // 0 for Sunday, 1 for Monday...
        val daysInMonth = yearMonth.lengthOfMonth()

        val calendarDays = mutableListOf<CalendarDayUiModel>()

        // Leading empty slots for days before 1st of month
        for (i in 0 until firstDayOfWeek) {
            val prevDate = firstOfMonth.minusDays((firstDayOfWeek - i).toLong())
            calendarDays.add(
                CalendarDayUiModel(
                    date = prevDate,
                    dayOfMonth = prevDate.dayOfMonth,
                    isCurrentMonth = false,
                    isSelected = false,
                    isToday = false,
                    hasTasks = false
                )
            )
        }

        // Days in current month
        for (day in 1..daysInMonth) {
            val date = yearMonth.atDay(day)
            calendarDays.add(
                CalendarDayUiModel(
                    date = date,
                    dayOfMonth = day,
                    isCurrentMonth = true,
                    isSelected = date == selected,
                    isToday = date == today,
                    hasTasks = datesWithTasks.contains(date)
                )
            )
        }

        // Tasks for selected date
        val selectedTasks = (tasksByDate[selected] ?: emptyList())
            .sortedWith(compareBy<Task> { it.dueTime }.thenBy { it.id })
            .map { it.toCalendarTaskUiModel() }

        return CalendarUiState(
            currentYearMonth = yearMonth,
            selectedDate = selected,
            monthTitleFormatted = monthTitle,
            scheduleHeaderFormatted = scheduleHeader,
            calendarDays = calendarDays,
            scheduledTasks = selectedTasks,
            isLoading = false
        )
    }

    private fun Task.toCalendarTaskUiModel(): CalendarTaskUiModel {
        val timeSource = if (dueTime > 0L) dueTime else dueDate
        val timeDisplayFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeFullFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val priorityText = when (priority) {
            Priority.LOW -> "Low"
            Priority.MEDIUM -> "Medium"
            Priority.HIGH -> "High"
            Priority.URGENT -> "Urgent"
        }

        return CalendarTaskUiModel(
            id = id.toLong(),
            title = title,
            dueTimeFormatted = timeFullFormat.format(Date(timeSource)),
            timeDisplay = timeDisplayFormat.format(Date(timeSource)),
            priorityLabel = priorityText,
            priority = priority.name,
            isCompleted = isCompleted
        )
    }
}
