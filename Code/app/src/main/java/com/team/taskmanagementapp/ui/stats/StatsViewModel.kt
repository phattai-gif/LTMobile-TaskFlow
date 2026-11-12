package com.team.taskmanagementapp.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.taskmanagementapp.data.local.dao.PomodoroDao
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class StatsViewModel(
    private val taskRepository: TaskRepository,
    private val pomodoroDao: PomodoroDao
) : ViewModel() {

    private val now = System.currentTimeMillis()
    private val todayStart = startOfDay(now)
    private val todayEnd = endOfDay(now)
    private val weekStart = startOfWeek(now)
    private val weekEnd = endOfWeek(now)

    val uiState: StateFlow<StatsUiState> = combine(
        taskRepository.getAllTasks(),
        pomodoroDao.observeTotalFocusMinutesInRange(todayStart, todayEnd),
        pomodoroDao.observeTotalFocusMinutesInRange(weekStart, weekEnd)
    ) { tasks, todayFocusMin, weekFocusMin ->
        buildStatsUiState(tasks, todayFocusMin, weekFocusMin)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(isLoading = true)
    )

    private fun buildStatsUiState(
        tasks: List<Task>,
        todayFocusMin: Int,
        weekFocusMin: Int
    ): StatsUiState {
        // 1. Weekday completions (Mon to Sun of current week)
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val completionsByDay = mutableListOf<WeekdayCompletion>()

        val cal = Calendar.getInstance().apply {
            timeInMillis = weekStart
        }

        for (i in 0 until 7) {
            val dayStartTime = startOfDay(cal.timeInMillis)
            val dayEndTime = endOfDay(cal.timeInMillis)

            val count = tasks.count { task ->
                task.isCompleted && (
                    (task.completedAt != null && task.completedAt in dayStartTime..dayEndTime) ||
                    (task.completedAt == null && task.dueDate in dayStartTime..dayEndTime)
                )
            }

            completionsByDay.add(WeekdayCompletion(dayLabels[i], count))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        val maxCount = maxOf(12, completionsByDay.maxOfOrNull { it.count } ?: 0)

        // 2. Completion rate
        val totalTasks = tasks.size
        val completedCount = tasks.count { it.isCompleted }
        val completionRate = if (totalTasks > 0) (completedCount * 100 / totalTasks) else 0

        val rateStatus = when {
            totalTasks == 0 -> "No tasks"
            completionRate >= 70 -> "On track"
            else -> "In progress"
        }

        // 3. This week completed & not completed
        val completedThisWeek = tasks.count { task ->
            task.isCompleted && (
                (task.completedAt != null && task.completedAt in weekStart..weekEnd) ||
                (task.completedAt == null && task.dueDate in weekStart..weekEnd)
            )
        }

        val notCompletedThisWeek = tasks.count { task ->
            !task.isCompleted && task.dueDate in weekStart..weekEnd
        }

        // 4. Tasks by Priority
        val urgent = tasks.count { it.priority == Priority.URGENT }
        val high = tasks.count { it.priority == Priority.HIGH }
        val medium = tasks.count { it.priority == Priority.MEDIUM }
        val low = tasks.count { it.priority == Priority.LOW }

        val priorityDistribution = PriorityDistribution(
            urgentCount = urgent,
            highCount = high,
            mediumCount = medium,
            lowCount = low,
            totalCount = totalTasks
        )

        // 5. Pomodoro stats
        // Fallback to task.totalFocusTimeMinutes if no session entries
        val tasksFocusTotal = tasks.sumOf { it.totalFocusTimeMinutes }
        val effectiveTodayFocus = maxOf(todayFocusMin, 0)
        val effectiveWeekFocus = maxOf(weekFocusMin, tasksFocusTotal)

        val pomodoroStats = PomodoroStats(
            todayMinutes = effectiveTodayFocus,
            thisWeekMinutes = effectiveWeekFocus
        )

        return StatsUiState(
            weekdayCompletions = completionsByDay,
            maxWeekdayCount = maxCount,
            completionRatePercent = completionRate,
            completionRateStatus = rateStatus,
            completedThisWeek = completedThisWeek,
            notCompletedThisWeek = notCompletedThisWeek,
            priorityDistribution = priorityDistribution,
            pomodoroStats = pomodoroStats,
            isLoading = false
        )
    }

    private fun startOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun endOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun startOfWeek(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun endOfWeek(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}
