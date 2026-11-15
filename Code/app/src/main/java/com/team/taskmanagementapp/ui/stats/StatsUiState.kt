package com.team.taskmanagementapp.ui.stats

import com.team.taskmanagementapp.data.model.stats.StatsTimeFilter
import com.team.taskmanagementapp.data.model.stats.TaskFocusSummary

data class WeekdayCompletion(
    val dayLabel: String, // "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    val count: Int
)

data class PriorityDistribution(
    val urgentCount: Int = 0,
    val highCount: Int = 0,
    val mediumCount: Int = 0,
    val lowCount: Int = 0,
    val totalCount: Int = 0
)

data class PomodoroStats(
    val todayMinutes: Int = 0,
    val thisWeekMinutes: Int = 0,
    val periodMinutes: Int = 0,
    val periodSessionCount: Int = 0,
    val topTasks: List<TaskFocusSummary> = emptyList()
) {
    val hasFocus: Boolean
        get() = periodSessionCount > 0 || todayMinutes > 0 || thisWeekMinutes > 0
}

data class StatsUiState(
    val timeFilter: StatsTimeFilter = StatsTimeFilter.THIS_WEEK,
    val weekdayCompletions: List<WeekdayCompletion> = emptyList(),
    val maxWeekdayCount: Int = 12,
    val completionRatePercent: Int = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val hasUnknownCompletionDates: Boolean = false,
    val priorityDistribution: PriorityDistribution = PriorityDistribution(),
    val pomodoroStats: PomodoroStats = PomodoroStats(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false
) {
    val isEmpty: Boolean
        get() = !isLoading && !hasError &&
            priorityDistribution.totalCount == 0 && !pomodoroStats.hasFocus
}
