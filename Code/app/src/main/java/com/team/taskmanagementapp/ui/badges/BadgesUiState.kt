package com.team.taskmanagementapp.ui.badges

data class BadgeItemUiModel(
    val id: String,
    val iconEmoji: String,
    val title: String,
    val description: String? = null,
    val isUnlocked: Boolean = false
)

data class BadgesUiState(
    val unlockedCount: Int = 0,
    val totalBadgesCount: Int = 21,
    val streakBadges: List<BadgeItemUiModel> = emptyList(),
    val taskBadges: List<BadgeItemUiModel> = emptyList(),
    val specialBadges: List<BadgeItemUiModel> = emptyList(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 1,
    val todayCompletedCount: Int = 0,
    val todayTotalCount: Int = 0,
    val totalCompletedTasksCount: Int = 0,
    val isLoading: Boolean = false
)
