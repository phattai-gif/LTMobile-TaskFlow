package com.team.taskmanagementapp.ui.streak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.data.repository.TaskRepository
import com.team.taskmanagementapp.ui.home.StreakDayUiModel
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
        val weekDays = buildStreakWeekDays(now)
        val (currentStreak, bestStreak) = calculateStreak(tasks)
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
            weekDays = buildStreakWeekDays(System.currentTimeMillis()),
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

    private fun calculateStreak(tasks: List<Task>): Pair<Int, Int> {
        val zoneId = ZoneId.systemDefault()
        val completedDates = tasks
            .filter { it.isCompleted || it.status == TaskStatus.COMPLETED }
            .mapNotNull { it.completedAt ?: it.dueDate.takeIf { d -> d > 0L } }
            .map { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() }
            .toSet()

        val today = LocalDate.now(zoneId)
        var streak = 0
        var checkDate = if (completedDates.contains(today)) today else today.minusDays(1)

        while (completedDates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        val sortedDates = completedDates.sorted()
        var maxRun = 0
        var run = 0
        var prevDate: LocalDate? = null
        for (date in sortedDates) {
            if (prevDate == null || date == prevDate.plusDays(1)) {
                run++
            } else if (date != prevDate) {
                run = 1
            }
            if (run > maxRun) maxRun = run
            prevDate = date
        }
        val best = maxOf(streak, maxRun, 1)

        return Pair(streak, best)
    }

    private fun buildStreakWeekDays(now: Long): List<StreakDayUiModel> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            firstDayOfWeek = Calendar.MONDAY
        }
        val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dayLetters = listOf("T", "T", "T", "T", "T", "T", "C")
        val days = mutableListOf<StreakDayUiModel>()

        for (i in 0 until 7) {
            val dayNum = calendar.get(Calendar.DAY_OF_MONTH)
            val isToday = calendar.get(Calendar.DAY_OF_YEAR) == currentDayOfYear
            days.add(
                StreakDayUiModel(
                    dayLetter = dayLetters[i],
                    dayNumber = dayNum,
                    isToday = isToday,
                    isCompleted = false
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
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
