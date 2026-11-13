package com.team.taskmanagementapp.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.repository.PomodoroSettingsRepository
import com.team.taskmanagementapp.data.repository.TaskRepository
import com.team.taskmanagementapp.pomodoro.PomodoroConfig
import com.team.taskmanagementapp.pomodoro.PomodoroService
import com.team.taskmanagementapp.pomodoro.PomodoroTimerController
import com.team.taskmanagementapp.pomodoro.PomodoroTimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal class PomodoroViewModel(
    taskRepository: TaskRepository,
    private val settings: PomodoroSettingsRepository,
    private val appContext: Context,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val _message = MutableStateFlow<Int?>(null)
    val message = _message.asStateFlow()

    val timer = PomodoroTimerController.state
    val tasks = taskRepository.getAllTasks()
        .catch { error ->
            Log.e(TAG, "Unable to load tasks for Pomodoro", error)
            _message.value = R.string.pomodoro_tasks_load_error
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val pendingTaskId = savedState.getStateFlow<Long?>(SELECTED_TASK_ID, null)

    val selectedTask = combine(timer, pendingTaskId, tasks) { snapshot, pendingId, list ->
        val id = snapshot.taskId ?: pendingId
        list.firstOrNull { it.id.toLong() == id }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _config = MutableStateFlow(settings.load())
    val config = _config.asStateFlow()

    init {
        PomodoroTimerController.engine.updateConfig(_config.value)
    }

    fun selectTask(task: Task) {
        if (timer.value.state == PomodoroTimerState.IDLE && tasks.value.any { it.id == task.id }) {
            savedState[SELECTED_TASK_ID] = task.id.toLong()
        }
    }

    /** Returns false when the task picker needs to open. */
    fun onPrimaryAction(): Boolean {
        val snapshot = timer.value
        when (snapshot.state) {
            PomodoroTimerState.IDLE -> {
                val id = pendingTaskId.value
                if (id == null || tasks.value.none { it.id.toLong() == id }) return false
                sendCommand { PomodoroService.start(appContext, id) }
            }
            PomodoroTimerState.PAUSED, PomodoroTimerState.COMPLETED -> resume()
            PomodoroTimerState.RUNNING -> Unit
        }
        return true
    }

    fun pause() = sendCommand { PomodoroService.pause(appContext) }

    fun resume() = sendCommand { PomodoroService.resume(appContext) }

    fun skip() = sendCommand { PomodoroService.skip(appContext) }

    fun stop() = sendCommand { PomodoroService.stop(appContext) }

    fun updateConfig(change: (PomodoroConfig) -> PomodoroConfig) {
        val normalized = change(_config.value).withValidDurations()
        settings.save(normalized)
        _config.value = normalized
        PomodoroTimerController.engine.updateConfig(normalized)
    }

    fun dismissMessage() {
        _message.value = null
    }

    private inline fun sendCommand(action: () -> Unit) {
        try {
            action()
        } catch (error: IllegalStateException) {
            showCommandError(error)
        } catch (error: SecurityException) {
            showCommandError(error)
        }
    }

    private fun showCommandError(error: RuntimeException) {
        Log.e(TAG, "Unable to send Pomodoro timer command", error)
        _message.value = R.string.pomodoro_command_error
    }

    private companion object {
        const val TAG = "PomodoroViewModel"
        const val SELECTED_TASK_ID = "pomodoro_selected_task_id"
    }
}
