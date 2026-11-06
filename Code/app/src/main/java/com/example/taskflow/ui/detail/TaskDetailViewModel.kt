package com.example.taskflow.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.util.DateTimeUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val taskId: Long,
    private val taskSource: TaskDetailSource,
    private val now: () -> Long = System::currentTimeMillis
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        retry()
    }

    fun retry() {
        observeJob?.cancel()
        _uiState.update { it.copy(isLoading = true, hasLoadError = false) }
        observeJob = viewModelScope.launch {
            try {
                taskSource.observeTask(taskId).collect { task ->
                    _uiState.update {
                        if (it.isDeleted) it else it.copy(
                            task = task,
                            isLoading = false,
                            hasLoadError = false
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, hasLoadError = true)
                }
            }
        }
    }

    fun toggleComplete() = performAction(TaskDetailActionError.COMPLETE) { task ->
        val timestamp = now()
        val wasCompleted = task.isCompleted || task.status == TaskStatus.COMPLETED
        val dueAt = DateTimeUtils.getCombinedDueTimestamp(task.dueDate, task.dueTime)
        val updated = task.copy(
            isCompleted = !wasCompleted,
            status = when {
                !wasCompleted -> TaskStatus.COMPLETED
                dueAt > 0L && dueAt < timestamp -> TaskStatus.OVERDUE
                else -> TaskStatus.TODO
            },
            completedAt = if (wasCompleted) null else timestamp,
            updatedAt = timestamp
        )
        taskSource.updateTask(updated)
        _uiState.update { it.copy(task = updated) }
    }

    /** Sửa nhanh nội dung; giữ các trường lịch nhắc, lặp lại và lịch sử tập trung. */
    fun saveContent(title: String, description: String) {
        if (title.isBlank()) return
        performAction(TaskDetailActionError.EDIT) { task ->
            val updated = task.copy(
                title = title.trim(),
                description = description.trim(),
                updatedAt = now()
            )
            taskSource.updateTask(updated)
            _uiState.update { it.copy(task = updated, editSaved = true) }
        }
    }

    /** Chỉ xóa công việc hiện tại, không xóa các kỳ khác của chuỗi lặp. */
    fun deleteTask() = performAction(TaskDetailActionError.DELETE) { task ->
        taskSource.deleteTask(task)
        _uiState.update { it.copy(isDeleted = true) }
    }

    fun clearActionFeedback() {
        _uiState.update { it.copy(actionError = null, editSaved = false) }
    }

    private fun performAction(
        action: TaskDetailActionError,
        operation: suspend (Task) -> Unit
    ) {
        val state = _uiState.value
        if (state.isBusy || state.isLoading || state.hasLoadError ||
            state.isDeleted || state.task == null
        ) return

        // Đặt cờ trước khi launch để chặn thao tác bấm liên tiếp.
        _uiState.update { it.copy(isBusy = true, actionError = null, editSaved = false) }
        viewModelScope.launch {
            try {
                // Đọc lại để không ghi đè thay đổi từ màn hình khác bằng dữ liệu cũ.
                val latest = taskSource.getTask(taskId)
                if (latest == null) {
                    _uiState.update { it.copy(task = null) }
                } else {
                    operation(latest)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.update { it.copy(actionError = action) }
            } finally {
                _uiState.update { it.copy(isBusy = false) }
            }
        }
    }
}
