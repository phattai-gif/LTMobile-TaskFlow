package com.team.taskmanagementapp.ui.detail

import com.team.taskmanagementapp.data.local.entity.Task

enum class TaskDetailActionError {
    EDIT,
    COMPLETE,
    DELETE
}

data class TaskDetailUiState(
    val task: Task? = null,
    val isLoading: Boolean = true,
    val hasLoadError: Boolean = false,
    val isBusy: Boolean = false,
    val actionError: TaskDetailActionError? = null,
    val isDeleted: Boolean = false,
    val editSaved: Boolean = false
)
