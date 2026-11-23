package com.team.taskmanagementapp.ui.detail

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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.team.taskmanagementapp.ui.components.TaskFlowTextField
import com.team.taskmanagementapp.ui.theme.TaskFlowDimensions
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
    var titleValue by rememberSaveable(task.id, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(task.title, TextRange(task.title.length)))
    }
    var descriptionValue by rememberSaveable(task.id, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(task.description, TextRange(task.description.length)))
    }

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
                    value = titleValue,
                    onValueChange = { titleValue = it },
                    label = stringResource(R.string.task_title_label),
                    enabled = !isBusy,
                    errorMessage = if (titleValue.text.isBlank()) {
                        stringResource(R.string.task_detail_title_required)
                    } else null
                )
                TaskFlowTextField(
                    value = descriptionValue,
                    onValueChange = { descriptionValue = it },
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
                onClick = { onSave(titleValue.text, descriptionValue.text) },
                enabled = !isBusy && titleValue.text.isNotBlank()
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
