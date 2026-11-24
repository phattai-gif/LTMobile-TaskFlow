package com.team.taskmanagementapp.ui.streak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.repository.TaskRepository
import com.team.taskmanagementapp.ui.home.StreakDayUiModel
import com.team.taskmanagementapp.util.StreakCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale

class StreakViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val streakProtectionEnabled = MutableStateFlow(true)
    private val reminderHour = MutableStateFlow(20)
    private val reminderMinute = MutableStateFlow(0)

    val uiState: StateFlow<StreakUiState> = combine(
        taskRepository.getAllTasks(),
        streakProtectionEnabled,
        reminderHour,
        reminderMinute
    ) { tasks: List<Task>, protectionEnabled: Boolean, hour: Int, minute: Int ->
        val now = System.currentTimeMillis()
        val streakResult = StreakCalculator.calculateForEntities(tasks, now)
        val currentStreak = streakResult.currentStreak
        val bestStreak = streakResult.bestStreak
        val weekDays = streakResult.weekDays
        val badgeName = when {
            currentStreak >= 7 -> "Gold Badge"
            currentStreak >= 4 -> "Silver Badge"
            currentStreak >= 1 -> "Bronze Badge"
            else -> "None Badge"
        }

        StreakUiState(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            badgeName = badgeName,
            weekDays = weekDays,
            streakProtectionEnabled = protectionEnabled,
            reminderHour = hour,
            reminderMinute = minute,
            reminderTimeFormatted = formatReminderTime(hour, minute),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StreakUiState(
            weekDays = StreakCalculator.buildStreakWeekDays(System.currentTimeMillis()),
            isLoading = true
        )
    )

    fun toggleStreakProtection(enabled: Boolean) {
        streakProtectionEnabled.value = enabled
    }

    fun setReminderTime(hour: Int, minute: Int) {
        reminderHour.value = hour
        reminderMinute.value = minute
    }

    private fun formatReminderTime(hour: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        return sdf.format(cal.time)
    }
}
