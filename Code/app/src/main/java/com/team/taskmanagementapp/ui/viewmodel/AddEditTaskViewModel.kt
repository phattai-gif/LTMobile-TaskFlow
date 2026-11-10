package com.team.taskmanagementapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.RecurrenceType
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.data.repository.TaskRepository
import kotlinx.coroutines.launch

sealed class AddEditTaskState {
    object Idle : AddEditTaskState()
    object Loading : AddEditTaskState()
    object Saving : AddEditTaskState()
    data class Loaded(val task: Task) : AddEditTaskState()
    data class Saved(val task: Task) : AddEditTaskState()
    data class Error(val message: String) : AddEditTaskState()
}

class AddEditTaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _state = MutableLiveData<AddEditTaskState>(AddEditTaskState.Idle)
    val state: LiveData<AddEditTaskState> = _state

    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            _state.value = AddEditTaskState.Loading

            try {
                val task = repository.getTaskById(taskId)

                _state.value = if (task != null) {
                    AddEditTaskState.Loaded(task)
                } else {
                    AddEditTaskState.Error("Không tìm thấy công việc.")
                }
            } catch (exception: Exception) {
                _state.value = AddEditTaskState.Error(
                    exception.localizedMessage ?: "Không tải được công việc."
                )
            }
        }
    }

    fun saveTask(
        taskId: Long,
        title: String,
        description: String,
        dueDate: Long,
        dueTime: Long,
        priority: Priority,
        status: TaskStatus,
        recurrenceType: RecurrenceType,
        reminderMinutes: Int
    ) {
        viewModelScope.launch {
            _state.value = AddEditTaskState.Saving

            try {
                val now = System.currentTimeMillis()
                val trimmedTitle = title.trim()
                val trimmedDescription = description.trim()

                val savedTask = if (taskId > 0L) {
                    val existingTask = repository.getTaskById(taskId)
                        ?: throw IllegalStateException("Không tìm thấy công việc để cập nhật.")

                    val isCompleted = status == TaskStatus.COMPLETED
                    val wasCompleted =
                        existingTask.isCompleted ||
                                existingTask.status == TaskStatus.COMPLETED

                    val updatedTask = existingTask.copy(
                        title = trimmedTitle,
                        description = trimmedDescription,
                        dueDate = dueDate,
                        dueTime = dueTime,
                        priority = priority,
                        status = status,
                        isCompleted = isCompleted,
                        isRecurring = recurrenceType != RecurrenceType.NONE,
                        recurrenceType = recurrenceType,
                        reminderMinutes = reminderMinutes,
                        repeatEndDate = if (recurrenceType == RecurrenceType.NONE) {
                            0L
                        } else {
                            existingTask.repeatEndDate
                        },
                        repeatLimitCount = if (recurrenceType == RecurrenceType.NONE) {
                            0
                        } else {
                            existingTask.repeatLimitCount
                        },
                        currentOccurrence = if (recurrenceType == RecurrenceType.NONE) {
                            1
                        } else {
                            existingTask.currentOccurrence
                        },
                        isPaused = if (recurrenceType == RecurrenceType.NONE) {
                            false
                        } else {
                            existingTask.isPaused
                        },
                        updatedAt = maxOf(now, existingTask.updatedAt + 1L),
                        completedAt = when {
                            !isCompleted -> null
                            wasCompleted -> existingTask.completedAt
                            else -> now
                        }
                    )

                    repository.update(updatedTask)
                    updatedTask
                } else {
                    val newTask = Task(
                        title = trimmedTitle,
                        description = trimmedDescription,
                        dueDate = dueDate,
                        dueTime = dueTime,
                        priority = priority,
                        status = status,
                        isCompleted = status == TaskStatus.COMPLETED,
                        isRecurring = recurrenceType != RecurrenceType.NONE,
                        recurrenceType = recurrenceType,
                        reminderMinutes = reminderMinutes,
                        createdAt = now,
                        updatedAt = now,
                        completedAt = if (status == TaskStatus.COMPLETED) now else null
                    )

                    val insertedId = repository.insert(newTask)
                    newTask.copy(id = insertedId.toInt())
                }

                _state.value = AddEditTaskState.Saved(savedTask)
            } catch (exception: Exception) {
                _state.value = AddEditTaskState.Error(
                    exception.localizedMessage ?: "Không lưu được công việc."
                )
            }
        }
    }
}