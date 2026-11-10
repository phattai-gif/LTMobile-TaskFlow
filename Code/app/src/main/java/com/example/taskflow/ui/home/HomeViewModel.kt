package com.example.taskflow.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

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

    private fun observeHome(): Flow<HomeUiState> {
        return flow<HomeUiState> {
            emit(HomeUiState.Loading)

            emitAll(
                combine(
                    taskSource.observeTasks(),
                    dateChanges()
                ) { tasks, _ ->
                    createUiState(tasks)
                }
            )
        }.catch {
            emit(HomeUiState.Error)
        }
    }

    private fun createUiState(
        tasks: List<HomeTaskUiModel>
    ): HomeUiState {
        val todayStart = startOfDay(System.currentTimeMillis())

        val tomorrowStart = Calendar.getInstance().apply {
            timeInMillis = todayStart
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        val taskOrder = compareBy<HomeTaskUiModel> {
            startOfDay(it.dueDate)
        }.thenBy {
            minutesOfDay(it.dueTime)
        }.thenBy {
            it.id
        }

        val todayTasks = tasks
            .filter {
                it.dueDate >= todayStart &&
                        it.dueDate < tomorrowStart
            }
            .sortedWith(taskOrder)

        val upcomingTasks = tasks
            .filter {
                it.dueDate >= tomorrowStart
            }
            .sortedWith(taskOrder)

        return if (
            todayTasks.isEmpty() &&
            upcomingTasks.isEmpty()
        ) {
            HomeUiState.Empty
        } else {
            HomeUiState.Success(
                todayTasks = todayTasks,
                upcomingTasks = upcomingTasks
            )
        }
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