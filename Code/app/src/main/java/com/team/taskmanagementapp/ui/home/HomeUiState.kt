package com.team.taskmanagementapp.ui.home

data class HomeSummaryMetrics(
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val overdueCount: Int = 0,
    val weeklyAddedCount: Int = 0
)

data class StreakDayUiModel(
    val dayLetter: String, // "T", "T", "T", "T", "T", "T", "C"
    val dayNumber: Int,
    val isToday: Boolean,
    val isCompleted: Boolean
)

data class HomeStreakInfo(
    val currentStreak: Int = 0,
    val bestStreak: Int = 1,
    val weekDays: List<StreakDayUiModel> = emptyList()
)

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data object Empty : HomeUiState

    data object Error : HomeUiState

    data class Success(
        val metrics: HomeSummaryMetrics = HomeSummaryMetrics(),
        val streakInfo: HomeStreakInfo = HomeStreakInfo(),
        val todayTasks: List<HomeTaskUiModel> = emptyList(),
        val upcomingTasks: List<HomeTaskUiModel> = emptyList(),
        val completedTasks: List<HomeTaskUiModel> = emptyList(),
        val userName: String = "Alex"
    ) : HomeUiState
}

