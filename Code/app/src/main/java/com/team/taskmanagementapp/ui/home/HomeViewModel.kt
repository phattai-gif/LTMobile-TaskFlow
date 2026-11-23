package com.team.taskmanagementapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class HomeViewModel(
    private val taskSource: HomeTaskSource
) : ViewModel() {

    private val retrySignal = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = retrySignal
        .flatMapLatest {
            observeHome()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun retry() {
        retrySignal.update { it + 1 }
    }

    fun toggleTaskComplete(task: HomeTaskUiModel) {
        viewModelScope.launch {
            taskSource.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            taskSource.deleteTask(taskId)
        }
    }

    private fun authUserFlow(): Flow<com.google.firebase.auth.FirebaseUser?> = callbackFlow {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { fbAuth ->
            trySend(fbAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        // Also emit current value immediately
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private fun observeHome(): Flow<HomeUiState> {
        return flow<HomeUiState> {
            emit(HomeUiState.Loading)

            emitAll(
                combine(
                    taskSource.observeTasks(),
                    dateChanges(),
                    authUserFlow()
                ) { tasks, _, user ->
                    createUiState(tasks, user)
                }
            )
        }.catch {
            emit(HomeUiState.Error)
        }
    }

    private fun createUiState(
        tasks: List<HomeTaskUiModel>,
        user: com.google.firebase.auth.FirebaseUser?
    ): HomeUiState {
        val now = System.currentTimeMillis()
        val todayStart = startOfDay(now)

        val tomorrowStart = Calendar.getInstance().apply {
            timeInMillis = todayStart
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        val weekAgoStart = Calendar.getInstance().apply {
            timeInMillis = todayStart
            add(Calendar.DAY_OF_YEAR, -7)
        }.timeInMillis

        val totalCount = tasks.size
        val completedCount = tasks.count { it.isCompleted }
        val overdueCount = tasks.count { !it.isCompleted && it.dueDate > 0L && it.dueDate < todayStart }
        val pendingCount = tasks.count { !it.isCompleted && (it.dueDate <= 0L || it.dueDate >= todayStart) }
        val weeklyAddedCount = tasks.count { it.dueDate >= weekAgoStart }

        val metrics = HomeSummaryMetrics(
            totalCount = totalCount,
            completedCount = completedCount,
            pendingCount = pendingCount,
            overdueCount = overdueCount,
            weeklyAddedCount = weeklyAddedCount
        )

        val priorityComparator = compareByDescending<HomeTaskUiModel> {
            when (it.priority) {
                HomeTaskPriority.URGENT -> 3
                HomeTaskPriority.HIGH -> 2
                HomeTaskPriority.MEDIUM -> 1
                HomeTaskPriority.LOW -> 0
            }
        }

        val taskOrder = compareBy<HomeTaskUiModel> {
            startOfDay(it.dueDate)
        }.thenBy {
            minutesOfDay(it.dueTime)
        }.then(priorityComparator).thenBy {
            it.id
        }

        // Today's tasks: Incomplete tasks that are overdue or due today
        val todayTasks = tasks
            .filter {
                !it.isCompleted && (
                    (it.dueDate >= todayStart && it.dueDate < tomorrowStart) ||
                    (it.dueDate > 0L && it.dueDate < todayStart) ||
                    (it.dueDate <= 0L)
                )
            }
            .sortedWith(taskOrder)

        // Upcoming tasks: Incomplete tasks due tomorrow or later
        val upcomingTasks = tasks
            .filter {
                !it.isCompleted && it.dueDate >= tomorrowStart
            }
            .sortedWith(taskOrder)

        // Completed tasks
        val completedTasks = tasks
            .filter {
                it.isCompleted
            }
            .sortedByDescending { it.completedAt ?: it.dueDate }

        // Build streak week days (Monday to Sunday)
        val streakWeekDays = buildStreakWeekDays(now)
        val streakInfo = HomeStreakInfo(
            currentStreak = if (completedCount > 0) 1 else 0,
            bestStreak = 1,
            weekDays = streakWeekDays
        )

        val resolvedUserName = resolveUserName(user)

        return HomeUiState.Success(
            metrics = metrics,
            streakInfo = streakInfo,
            todayTasks = todayTasks,
            upcomingTasks = upcomingTasks,
            completedTasks = completedTasks,
            userName = resolvedUserName
        )
    }

    private fun resolveUserName(currentUser: com.google.firebase.auth.FirebaseUser?): String {
        val user = currentUser ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user == null) return ""
        val displayName = user.displayName?.trim()
        if (!displayName.isNullOrBlank()) {
            return displayName
        }
        val email = user.email?.trim() ?: ""
        if (email.isNotBlank()) {
            val prefix = email.substringBefore('@')
            val words = prefix.split('.', '_', '-').filter { it.isNotBlank() }
            if (words.size > 1) {
                return words.joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
            }
            return prefix.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
        return ""
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

    private fun startOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun minutesOfDay(timestamp: Long?): Int {
        if (timestamp == null || timestamp <= 0L) {
            return Int.MAX_VALUE
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        return calendar.get(Calendar.HOUR_OF_DAY) * 60 +
                calendar.get(Calendar.MINUTE)
    }

    private fun dateChanges(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)

            val nextMidnight = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val waitMillis = (
                    nextMidnight - System.currentTimeMillis()
                    ).coerceIn(1_000L, 60_000L)

            delay(waitMillis)
        }
    }
}