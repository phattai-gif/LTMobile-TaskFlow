package com.team.taskmanagementapp.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.FilterCriteria
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.RecurrenceType
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.data.repository.TaskRepository
import com.team.taskmanagementapp.ui.base.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository,
    context: Context? = null,
    private val preferences: SharedPreferences? = null
) : ViewModel() {

    private val applicationContext = context?.applicationContext

    private val _deleteSuccess = MutableSharedFlow<Boolean>()
    val deleteSuccess = _deleteSuccess.asSharedFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _uiState = MutableStateFlow<UiState<List<Task>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Task>>> = _uiState.asStateFlow()

    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    val allTasks: StateFlow<List<Task>> = _allTasks.asStateFlow()

    private val _totalCompletedTasks = MutableStateFlow<Int>(0)
    val totalCompletedTasks: StateFlow<Int> = _totalCompletedTasks.asStateFlow()

    private var taskListJob: Job? = null

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

    private val _filterCriteria = MutableStateFlow(FilterCriteria())
    val filterCriteria: StateFlow<FilterCriteria> = _filterCriteria.asStateFlow()

    init {
        viewModelScope.launch {
            // Chờ cập nhật trạng thái quá hạn trong database xong rồi mới tải danh sách.
            repository.checkAndUpdateOverdueTasks()
            loadAllTasks()
        }

        viewModelScope.launch {
            repository.getAllTasks().collect { tasks ->
                _allTasks.value = tasks
                _totalCompletedTasks.value = tasks.count {
                    it.isCompleted || it.status == TaskStatus.COMPLETED
                }
            }
        }
    }

    fun refreshStreak() {
        viewModelScope.launch {
            repository.checkAndUpdateOverdueTasks()
            val tasks = _allTasks.value
            _totalCompletedTasks.value = tasks.count {
                it.isCompleted || it.status == TaskStatus.COMPLETED
            }
        }
    }

    fun loadAllTasks() {
        taskListJob?.cancel()
        taskListJob = viewModelScope.launch {
            repository.checkAndUpdateOverdueTasks()
            _uiState.value = UiState.Loading

            repository.getFilteredTasks(_filterCriteria.value)
                .catch { exception ->
                    _uiState.value = UiState.Error(
                        "Unable to load tasks: ${exception.localizedMessage}",
                        exception
                    )
                }
                .collect { tasks ->
                    _uiState.value = if (tasks.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(tasks)
                    }
                }
        }
    }

    fun insertTask(task: Task) {
        viewModelScope.launch {
            try {
                val insertedId = repository.insert(task)
                val savedTask = task.copy(id = insertedId.toInt())
                _userMessage.emit("Added task \"${savedTask.title}\"")
            } catch (exception: Exception) {
                _uiState.value = UiState.Error(
                    "Error adding task: ${exception.localizedMessage}",
                    exception
                )
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.update(task)
                _userMessage.emit("Updated task \"${task.title}\"")
            } catch (exception: Exception) {
                _uiState.value = UiState.Error(
                    "Error updating task: ${exception.localizedMessage}",
                    exception
                )
            }
        }
    }

    fun deleteTask(task: Task, deleteAllFuture: Boolean = false) {
        viewModelScope.launch {
            try {
                if (
                    deleteAllFuture &&
                    task.isRecurring &&
                    task.recurrenceType != RecurrenceType.NONE
                ) {
                    repository.deleteFutureRecurringTasks(
                        task.title,
                        task.recurrenceType,
                        task.dueDate
                    )
                } else {
                    repository.delete(task)
                }

                _deleteSuccess.emit(true)
                _userMessage.emit("Deleted task \"${task.title}\"")
            } catch (exception: Exception) {
                _userMessage.emit(
                    "Error deleting task: ${exception.localizedMessage}"
                )
            }
        }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val wasCompleted = task.isCompleted

                val updatedTask = task.copy(
                    isCompleted = !wasCompleted,
                    completedAt = if (!wasCompleted) now else null,
                    status = if (!wasCompleted) {
                        TaskStatus.COMPLETED
                    } else {
                        TaskStatus.TODO
                    },
                    updatedAt = now
                )

                repository.update(updatedTask)

                if (_selectedTask.value?.id == task.id) {
                    _selectedTask.value = updatedTask
                }

                val message = if (!wasCompleted) {
                    "Completed \"${task.title}\""
                } else {
                    "Marked \"${task.title}\" as incomplete"
                }

                _userMessage.emit(message)
            } catch (exception: Exception) {
                _userMessage.emit(
                    "Error updating status: ${exception.localizedMessage}"
                )
            }
        }
    }

    fun searchTasks(query: String) {
        if (query.isBlank()) {
            loadAllTasks()
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            repository.search(query)
                .catch { exception ->
                    _uiState.value = UiState.Error(
                        "Search error: ${exception.localizedMessage}",
                        exception
                    )
                }
                .collect { tasks ->
                    _uiState.value = if (tasks.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(tasks)
                    }
                }
        }
    }

    fun applyFilter(criteria: FilterCriteria) {
        _filterCriteria.value = criteria
        loadAllTasks()
    }

    fun clearFilter() {
        _filterCriteria.value = FilterCriteria()
        loadAllTasks()
    }

    fun filterByStatus(status: TaskStatus) {
        applyFilter(_filterCriteria.value.copy(statuses = setOf(status)))
    }

    fun filterByPriority(priority: Priority) {
        applyFilter(_filterCriteria.value.copy(priorities = setOf(priority)))
    }

    fun getTaskById(taskId: Long) {
        viewModelScope.launch {
            repository.observeTaskById(taskId).collect { task ->
                _selectedTask.value = task
            }
        }
    }

    fun toggleTaskPause(task: Task) {
        viewModelScope.launch {
            try {
                val newPausedState = !task.isPaused
                val updatedTask = task.copy(
                    isPaused = newPausedState,
                    updatedAt = System.currentTimeMillis()
                )

                repository.update(updatedTask)

                if (_selectedTask.value?.id == task.id) {
                    _selectedTask.value = updatedTask
                }

                _userMessage.emit(
                    if (newPausedState) {
                        "Paused recurring task"
                    } else {
                        "Resumed recurring task"
                    }
                )
            } catch (exception: Exception) {
                _userMessage.emit("Error: ${exception.localizedMessage}")
            }
        }
    }
}
