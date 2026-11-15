package com.team.taskmanagementapp.ui.streak

import com.team.taskmanagementapp.ui.home.StreakDayUiModel

data class StreakUiState(
    val currentStreak: Int = 0,
    val bestStreak: Int = 1,
    val badgeName: String = "None Badge",
    val weekDays: List<StreakDayUiModel> = emptyList(),
    val streakProtectionEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val reminderTimeFormatted: String = "08:00 PM",
    val isLoading: Boolean = false
)
