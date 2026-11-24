package com.team.taskmanagementapp.util

import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.ui.home.HomeTaskStatus
import com.team.taskmanagementapp.ui.home.HomeTaskUiModel
import com.team.taskmanagementapp.ui.home.StreakDayUiModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

object StreakCalculator {

    data class StreakResult(
        val currentStreak: Int,
        val bestStreak: Int,
        val weekDays: List<StreakDayUiModel>
    )

    fun calculateFromCompletedTimestamps(
        completedTimestamps: List<Long>,
        now: Long = System.currentTimeMillis()
    ): Pair<Int, Int> {
        val zoneId = ZoneId.systemDefault()
        val completedDates = completedTimestamps
            .filter { it > 0L }
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
        val best = maxOf(streak, maxRun, if (streak > 0) streak else 0)

        return Pair(streak, best)
    }

    fun calculateForHomeTasks(
        tasks: List<HomeTaskUiModel>,
        now: Long = System.currentTimeMillis()
    ): StreakResult {
        val completedTimestamps = tasks
            .filter { it.isCompleted || it.status == HomeTaskStatus.COMPLETED }
            .mapNotNull { it.completedAt ?: it.dueDate.takeIf { d -> d > 0L } }

        val (currentStreak, bestStreak) = calculateFromCompletedTimestamps(completedTimestamps, now)
        val weekDays = buildStreakWeekDays(now)
        return StreakResult(currentStreak, bestStreak, weekDays)
    }

    fun calculateForEntities(
        tasks: List<Task>,
        now: Long = System.currentTimeMillis()
    ): StreakResult {
        val completedTimestamps = tasks
            .filter { it.isCompleted || it.status == TaskStatus.COMPLETED }
            .mapNotNull { it.completedAt ?: it.dueDate.takeIf { d -> d > 0L } }

        val (currentStreak, bestStreak) = calculateFromCompletedTimestamps(completedTimestamps, now)
        val weekDays = buildStreakWeekDays(now)
        return StreakResult(currentStreak, bestStreak, weekDays)
    }

    fun buildStreakWeekDays(now: Long): List<StreakDayUiModel> {
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
}
