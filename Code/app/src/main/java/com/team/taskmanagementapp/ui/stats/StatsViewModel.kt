package com.team.taskmanagementapp.ui.stats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.taskmanagementapp.data.local.dao.PomodoroDao
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.data.model.stats.StatsTimeFilter
import com.team.taskmanagementapp.data.model.stats.TaskFocusStats
import com.team.taskmanagementapp.data.model.stats.buildTopTaskSummaries
import com.team.taskmanagementapp.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    private val taskRepository: TaskRepository,
    private val pomodoroDao: PomodoroDao,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val retrySignal = MutableStateFlow(0)
    private val timeFilter = savedState.getStateFlow(FILTER_KEY, StatsTimeFilter.THIS_WEEK)

    val uiState: StateFlow<StatsUiState> = combine(timeFilter, retrySignal) { filter, _ -> filter }
        .flatMapLatest { filter ->
            flow {
                emit(StatsUiState(timeFilter = filter))
                emitAll(
                    localDays().flatMapLatest { (today, zone) ->
                        observeStats(filter, today, zone)
                    }
                )
            }.catch {
                emit(StatsUiState(timeFilter = filter, isLoading = false, hasError = true))
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            StatsUiState(timeFilter = timeFilter.value)
        )

    fun selectPeriod(filter: StatsTimeFilter) {
        savedState[FILTER_KEY] = filter
    }

    fun retry() {
        retrySignal.update { it + 1 }
    }

    private fun observeStats(
        filter: StatsTimeFilter,
        today: LocalDate,
        zone: ZoneId
    ): Flow<StatsUiState> {
        val todayRange = dateRange(today, today.plusDays(1), zone)
        val weekRange = statsPeriod(StatsTimeFilter.THIS_WEEK, today, zone)
        val period = statsPeriod(filter, today, zone)

        return combine(
            taskRepository.getAllTasks(),
            pomodoroDao.observeTotalFocusMinutesInRange(todayRange.first, todayRange.last),
            pomodoroDao.observeTotalFocusMinutesInRange(weekRange.first, weekRange.last),
            pomodoroDao.observeFocusStatsByTask(period.first, period.last)
        ) { tasks, todayMinutes, weekMinutes, focusByTask ->
            calculateStats(tasks, filter, period, zone, todayMinutes, weekMinutes, focusByTask)
        }
    }

    // Refresh the Room query bounds after midnight or a time zone change.
    private fun localDays(): Flow<Pair<LocalDate, ZoneId>> = flow {
        while (true) {
            val zone = ZoneId.systemDefault()
            emit(LocalDate.now(zone) to zone)
            delay(60_000)
        }
    }.distinctUntilChanged()

    private companion object {
        const val FILTER_KEY = "stats_time_filter"
    }
}

internal fun statsPeriod(
    filter: StatsTimeFilter,
    today: LocalDate,
    zone: ZoneId
): LongRange {
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    return when (filter) {
        StatsTimeFilter.THIS_WEEK -> dateRange(monday, monday.plusWeeks(1), zone)
        StatsTimeFilter.LAST_WEEK -> dateRange(monday.minusWeeks(1), monday, zone)
        StatsTimeFilter.THIS_MONTH -> {
            val first = today.withDayOfMonth(1)
            dateRange(first, first.plusMonths(1), zone)
        }
        StatsTimeFilter.ALL_TIME -> 0L..Long.MAX_VALUE
    }
}

private fun dateRange(start: LocalDate, endExclusive: LocalDate, zone: ZoneId): LongRange =
    start.atStartOfDay(zone).toInstant().toEpochMilli() until
        endExclusive.atStartOfDay(zone).toInstant().toEpochMilli()

internal fun calculateStats(
    tasks: List<Task>,
    filter: StatsTimeFilter,
    period: LongRange,
    zone: ZoneId,
    todayMinutes: Int,
    weekMinutes: Int,
    focusByTask: List<TaskFocusStats>
): StatsUiState {
    // Keep the source dashboard's period rules. A completed task is dated by its completion event.
    val tasksInPeriod = if (filter == StatsTimeFilter.ALL_TIME) tasks else tasks.filter { task ->
        val date = if (task.completed) task.completedAt else task.dueDate
        date != null && date in period
    }
    val completionCounts = tasksInPeriod.asSequence()
        .filter { it.completed }
        .mapNotNull { it.completedAt }
        .groupingBy { Instant.ofEpochMilli(it).atZone(zone).dayOfWeek }
        .eachCount()
    val weekdays = DayOfWeek.entries.map { day ->
        WeekdayCompletion(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()), completionCounts[day] ?: 0)
    }
    val total = tasksInPeriod.size
    val completed = tasksInPeriod.count { it.completed }
    val priorities = tasksInPeriod.groupingBy { it.priority }.eachCount()
    val highest = weekdays.maxOf { it.count }
    val chartStep = maxOf(4, (highest + 2) / 3)

    return StatsUiState(
        timeFilter = filter,
        weekdayCompletions = weekdays,
        maxWeekdayCount = chartStep * 3,
        completionRatePercent = if (total == 0) 0 else (completed * 100L / total).toInt(),
        completedCount = completed,
        pendingCount = total - completed,
        hasUnknownCompletionDates = tasks.any { it.completed && it.completedAt == null },
        priorityDistribution = PriorityDistribution(
            urgentCount = priorities[Priority.URGENT] ?: 0,
            highCount = priorities[Priority.HIGH] ?: 0,
            mediumCount = priorities[Priority.MEDIUM] ?: 0,
            lowCount = priorities[Priority.LOW] ?: 0,
            totalCount = total
        ),
        pomodoroStats = PomodoroStats(
            todayMinutes = todayMinutes,
            thisWeekMinutes = weekMinutes,
            periodMinutes = focusByTask.sumOf { it.totalMinutes },
            periodSessionCount = focusByTask.sumOf { it.sessionCount },
            topTasks = buildTopTaskSummaries(focusByTask, tasks)
        ),
        isLoading = false
    )
}

private val Task.completed: Boolean
    get() = isCompleted || status == TaskStatus.COMPLETED
