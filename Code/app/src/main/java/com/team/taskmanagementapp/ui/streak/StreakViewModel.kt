package com.team.taskmanagementapp.ui.streak

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.repository.TaskRepository
import com.team.taskmanagementapp.ui.home.StreakDayUiModel
import com.team.taskmanagementapp.util.StreakCalculator
import com.team.taskmanagementapp.util.StreakProtectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StreakViewModel(
    private val context: Context,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val streakProtectionEnabled = MutableStateFlow(StreakProtectionManager.isEnabled(context))
    private val reminderHour = MutableStateFlow(StreakProtectionManager.getReminderHour(context))
    private val reminderMinute = MutableStateFlow(StreakProtectionManager.getReminderMinute(context))

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
            streakProtectionEnabled = StreakProtectionManager.isEnabled(context),
            reminderHour = StreakProtectionManager.getReminderHour(context),
            reminderMinute = StreakProtectionManager.getReminderMinute(context),
            reminderTimeFormatted = formatReminderTime(
                StreakProtectionManager.getReminderHour(context),
                StreakProtectionManager.getReminderMinute(context)
            ),
            isLoading = true
        )
    )

    fun toggleStreakProtection(enabled: Boolean) {
        streakProtectionEnabled.value = enabled
        StreakProtectionManager.setEnabled(context, enabled)
    }

    fun setReminderTime(hour: Int, minute: Int) {
        reminderHour.value = hour
        reminderMinute.value = minute
        StreakProtectionManager.setReminderTime(context, hour, minute)
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
