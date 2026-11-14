package com.team.taskmanagementapp.ui.badges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

class BadgesViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<BadgesUiState> = taskRepository.getAllTasks()
        .map { tasks ->
            buildBadgesUiState(tasks)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BadgesUiState(isLoading = true)
        )

    private fun buildBadgesUiState(tasks: List<Task>): BadgesUiState {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)

        // Streak calculation
        val completedDates = tasks
            .filter { it.isCompleted || it.status == TaskStatus.COMPLETED }
            .mapNotNull { it.completedAt ?: it.dueDate.takeIf { d -> d > 0L } }
            .map { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() }
            .toSet()

        var currentStreak = 0
        var checkDate = if (completedDates.contains(today)) today else today.minusDays(1)
        while (completedDates.contains(checkDate)) {
            currentStreak++
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
        val bestStreak = maxOf(currentStreak, maxRun, 1)

        // Total completed count
        val totalCompletedCount = tasks.count { it.isCompleted || it.status == TaskStatus.COMPLETED }

        // Today tasks
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val tomorrowStart = todayStart + 24 * 60 * 60 * 1000L

        val todayTasks = tasks.filter {
            it.dueDate in todayStart until tomorrowStart ||
                (it.dueDate > 0L && it.dueDate < todayStart && !it.isCompleted)
        }
        val todayCompletedCount = todayTasks.count { it.isCompleted || it.status == TaskStatus.COMPLETED }
        val todayTotalCount = todayTasks.size

        // Streak Badges (7 items)
        val streakBadges = listOf(
            BadgeItemUiModel("s1", "🌱", "Starter - 3 Days", isUnlocked = bestStreak >= 3),
            BadgeItemUiModel("s2", "🌟", "Sparkstarter - 7 Days", isUnlocked = bestStreak >= 7),
            BadgeItemUiModel("s3", "🔥", "Streaker - 30 Days", isUnlocked = bestStreak >= 30),
            BadgeItemUiModel("s4", "⚡", "Achiever - 50 Days", isUnlocked = bestStreak >= 50),
            BadgeItemUiModel("s5", "💎", "Champion - 100 Days", isUnlocked = bestStreak >= 100),
            BadgeItemUiModel("s6", "🏆", "Legend - 200 Days", isUnlocked = bestStreak >= 200),
            BadgeItemUiModel("s7", "👑", "Master - 365 Days", isUnlocked = bestStreak >= 365)
        )

        // Task Completion Badges (7 items)
        val taskBadges = listOf(
            BadgeItemUiModel("t1", "🌱", "Task Novice - 10 Tasks", isUnlocked = totalCompletedCount >= 10),
            BadgeItemUiModel("t2", "📝", "Task Doer - 50 Tasks", isUnlocked = totalCompletedCount >= 50),
            BadgeItemUiModel("t3", "⚡", "Task Achiever - 100 Tasks", isUnlocked = totalCompletedCount >= 100),
            BadgeItemUiModel("t4", "🎯", "Task Executor - 250 Tasks", isUnlocked = totalCompletedCount >= 250),
            BadgeItemUiModel("t5", "💎", "Task Expert - 500 Tasks", isUnlocked = totalCompletedCount >= 500),
            BadgeItemUiModel("t6", "🏆", "Task Champion - 1000 Tasks", isUnlocked = totalCompletedCount >= 1000),
            BadgeItemUiModel("t7", "👑", "Task Master - 2000 Tasks", isUnlocked = totalCompletedCount >= 2000)
        )

        // Special Badges (7 items)
        val completedHardTasks = tasks.count {
            (it.isCompleted || it.status == TaskStatus.COMPLETED) &&
                (it.priority == Priority.HIGH || it.priority == Priority.URGENT)
        }
        val earlyBirdCount = tasks.count {
            (it.isCompleted || it.status == TaskStatus.COMPLETED) && it.dueTime > 0L &&
                Calendar.getInstance().apply { timeInMillis = it.dueTime }.get(Calendar.HOUR_OF_DAY) < 9
        }
        val nightOwlCount = tasks.count {
            (it.isCompleted || it.status == TaskStatus.COMPLETED) && it.dueTime > 0L &&
                Calendar.getInstance().apply { timeInMillis = it.dueTime }.get(Calendar.HOUR_OF_DAY) >= 21
        }

        val specialBadges = listOf(
            BadgeItemUiModel(
                id = "sp1",
                iconEmoji = "🔄",
                title = "Comeback Kid",
                description = "Recover from a broken streak and reach 7 consecutive days again",
                isUnlocked = bestStreak >= 7 && currentStreak >= 7
            ),
            BadgeItemUiModel(
                id = "sp2",
                iconEmoji = "💪",
                title = "Hard Worker",
                description = "Complete 10 Hard Tasks",
                isUnlocked = completedHardTasks >= 10
            ),
            BadgeItemUiModel(
                id = "sp3",
                iconEmoji = "🌅",
                title = "Early Bird",
                description = "Complete 50 tasks before 09:00 AM",
                isUnlocked = earlyBirdCount >= 50
            ),
            BadgeItemUiModel(
                id = "sp4",
                iconEmoji = "🌙",
                title = "Night Owl",
                description = "Complete 50 tasks after 09:00 PM",
                isUnlocked = nightOwlCount >= 50
            ),
            BadgeItemUiModel(
                id = "sp5",
                iconEmoji = "📅",
                title = "Month Conqueror",
                description = "Complete at least 1 task on every day of a full calendar month",
                isUnlocked = completedDates.size >= 28
            ),
            BadgeItemUiModel(
                id = "sp6",
                iconEmoji = "🚀",
                title = "Unstoppable",
                description = "Complete 100 tasks within 7 consecutive days",
                isUnlocked = totalCompletedCount >= 100
            ),
            BadgeItemUiModel(
                id = "sp7",
                iconEmoji = "👑",
                title = "Ultimate Master",
                description = "Reach both 365-day Streak Master and 2,000-task Task Master",
                isUnlocked = bestStreak >= 365 && totalCompletedCount >= 2000
            )
        )

        val unlockedCount = streakBadges.count { it.isUnlocked } +
            taskBadges.count { it.isUnlocked } +
            specialBadges.count { it.isUnlocked }

        return BadgesUiState(
            unlockedCount = unlockedCount,
            totalBadgesCount = 21,
            streakBadges = streakBadges,
            taskBadges = taskBadges,
            specialBadges = specialBadges,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            todayCompletedCount = todayCompletedCount,
            todayTotalCount = todayTotalCount,
            totalCompletedTasksCount = totalCompletedCount,
            isLoading = false
        )
    }
}
