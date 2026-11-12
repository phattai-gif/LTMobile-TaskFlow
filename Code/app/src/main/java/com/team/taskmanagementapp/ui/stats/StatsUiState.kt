package com.team.taskmanagementapp.ui.stats

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
    val thisWeekMinutes: Int = 0
) {
    val todayFormatted: String
        get() = formatMinutes(todayMinutes)

    val thisWeekFormatted: String
        get() = formatMinutes(thisWeekMinutes)

    private fun formatMinutes(minutes: Int): String {
        if (minutes <= 0) return "0m"
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}

data class StatsUiState(
    val weekdayCompletions: List<WeekdayCompletion> = listOf(
        WeekdayCompletion("Mon", 0),
        WeekdayCompletion("Tue", 0),
        WeekdayCompletion("Wed", 0),
        WeekdayCompletion("Thu", 0),
        WeekdayCompletion("Fri", 0),
        WeekdayCompletion("Sat", 0),
        WeekdayCompletion("Sun", 0)
    ),
    val maxWeekdayCount: Int = 12,
    val completionRatePercent: Int = 0,
    val completionRateStatus: String = "No tasks",
    val completedThisWeek: Int = 0,
    val notCompletedThisWeek: Int = 0,
    val priorityDistribution: PriorityDistribution = PriorityDistribution(),
    val pomodoroStats: PomodoroStats = PomodoroStats(),
    val isLoading: Boolean = false
)
