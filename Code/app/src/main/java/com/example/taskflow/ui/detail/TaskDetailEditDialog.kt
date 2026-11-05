package com.example.taskflow.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.taskflow.ui.components.TaskFlowTextField
import com.example.taskflow.ui.theme.TaskFlowDimensions
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.data.local.entity.Task

@Composable
fun TaskDetailEditDialog(
    task: Task,
    isBusy: Boolean,
    hasError: Boolean,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by rememberSaveable(task.id) { mutableStateOf(task.title) }
    var description by rememberSaveable(task.id) { mutableStateOf(task.description) }

    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        shape = MaterialTheme.shapes.large,
        title = { Text(stringResource(R.string.task_detail_edit_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(TaskFlowDimensions.spacing12)
            ) {
                TaskFlowTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = stringResource(R.string.task_title_label),
                    enabled = !isBusy,
                    errorMessage = if (title.isBlank()) {
                        stringResource(R.string.task_detail_title_required)
                    } else null
                )
                TaskFlowTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(R.string.task_description_label),
                    singleLine = false,
                    enabled = !isBusy
                )
                if (hasError) {
                    Text(
                        text = stringResource(R.string.task_detail_edit_error),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, description) },
                enabled = !isBusy && title.isNotBlank()
            ) {
                Text(stringResource(R.string.task_detail_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
