package com.example.taskflow.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskflow.ui.components.TaskFlowConfirmDialog
import com.team.taskmanagementapp.R

@Composable
fun TaskDetailRoute(
    taskId: Long,
    taskSource: TaskDetailSource,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detailViewModel = viewModel<TaskDetailViewModel>(key = "task-detail-$taskId") {
        TaskDetailViewModel(taskId, taskSource)
    }
    val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable(taskId) { mutableStateOf(false) }
    var showEditDialog by rememberSaveable(taskId) { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleted) {
        // Chỉ trở về sau khi thao tác xóa thực sự thành công.
        if (uiState.isDeleted) onBack()
    }
    LaunchedEffect(uiState.editSaved) {
        if (uiState.editSaved) {
            showEditDialog = false
            detailViewModel.clearActionFeedback()
        }
    }

    TaskDetailContent(
        uiState = uiState,
        onBack = onBack,
        onEdit = {
            detailViewModel.clearActionFeedback()
            showEditDialog = true
        },
        onToggleComplete = detailViewModel::toggleComplete,
        onDelete = { showDeleteDialog = true },
        onRetry = detailViewModel::retry,
        onDismissError = detailViewModel::clearActionFeedback,
        modifier = modifier
    )

    val task = uiState.task
    if (showDeleteDialog && task != null && !uiState.isDeleted) {
        TaskFlowConfirmDialog(
            title = stringResource(R.string.task_detail_delete_title),
            message = stringResource(R.string.task_detail_delete_message, task.title),
            confirmTextRes = R.string.task_detail_delete,
            onConfirm = {
                showDeleteDialog = false
                detailViewModel.deleteTask()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
    if (showEditDialog && task != null && !uiState.isDeleted) {
        TaskDetailEditDialog(
            task = task,
            isBusy = uiState.isBusy,
            hasError = uiState.actionError == TaskDetailActionError.EDIT,
            onSave = detailViewModel::saveContent,
            onDismiss = {
                showEditDialog = false
                detailViewModel.clearActionFeedback()
            }
        )
    }
}
