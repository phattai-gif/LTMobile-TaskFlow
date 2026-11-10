package com.team.taskmanagementapp.ui.detail

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team.taskmanagementapp.ui.components.TaskFlowButton
import com.team.taskmanagementapp.ui.components.TaskFlowCard
import com.team.taskmanagementapp.ui.theme.TaskFlowDimensions
import com.team.taskmanagementapp.ui.theme.TaskFLowTheme
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.RecurrenceType
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.util.DateTimeUtils
import java.util.Date

@Composable
fun TaskDetailContent(
    uiState: TaskDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(TaskFlowDimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(TaskFlowDimensions.spacing16)
    ) {
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.action_back))
        }
        Text(
            text = stringResource(R.string.task_detail_heading),
            style = MaterialTheme.typography.headlineMedium
        )
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(TaskFlowDimensions.spacing16)
                ) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.task_detail_loading))
                }
            }

            uiState.hasLoadError -> {
                Text(
                    text = stringResource(R.string.task_detail_load_error),
                    color = MaterialTheme.colorScheme.error
                )
                TaskFlowButton(
                    text = stringResource(R.string.action_retry),
                    onClick = onRetry
                )
            }

            uiState.task == null -> Text(stringResource(R.string.task_detail_not_found))

            else -> TaskDetailCards(
                task = uiState.task,
                isBusy = uiState.isBusy,
                actionError = uiState.actionError,
                onEdit = onEdit,
                onToggleComplete = onToggleComplete,
                onDelete = onDelete,
                onDismissError = onDismissError,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }
}

@Composable
private fun TaskDetailCards(
    task: Task,
    isBusy: Boolean,
    actionError: TaskDetailActionError?,
    onEdit: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val completed = task.isCompleted || task.status == TaskStatus.COMPLETED
    val dateText = if (task.dueDate > 0L) {
        DateFormat.getDateFormat(context).format(Date(task.dueDate))
    } else stringResource(R.string.home_no_date)
    val timeText = if (task.dueTime > 0L) {
        DateFormat.getTimeFormat(context).format(Date(task.dueTime))
    } else stringResource(R.string.home_no_time)
    val displayedStatus = when {
        completed -> TaskStatus.COMPLETED
        DateTimeUtils.isOverdue(task) -> TaskStatus.OVERDUE
        else -> task.status
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(TaskFlowDimensions.spacing16)
    ) {
        TaskFlowCard(modifier = Modifier.fillMaxWidth()) {
            Text(task.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(R.string.task_description_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = task.description.ifBlank {
                    stringResource(R.string.task_detail_no_description)
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(TaskFlowDimensions.spacing12)) {
            DetailInfoCard(
                label = stringResource(R.string.task_detail_due_date),
                value = dateText,
                modifier = Modifier.weight(1f)
            )
            DetailInfoCard(
                label = stringResource(R.string.task_detail_due_time),
                value = timeText,
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(TaskFlowDimensions.spacing12)) {
            DetailInfoCard(
                label = stringResource(R.string.task_detail_priority),
                value = stringResource(when (task.priority) {
                    Priority.LOW -> R.string.task_priority_low
                    Priority.MEDIUM -> R.string.task_priority_medium
                    Priority.HIGH -> R.string.task_priority_high
                    Priority.URGENT -> R.string.task_priority_urgent
                }),
                modifier = Modifier.weight(1f)
            )
            DetailInfoCard(
                label = stringResource(R.string.task_detail_status),
                value = stringResource(when (displayedStatus) {
                    TaskStatus.TODO -> R.string.task_status_pending
                    TaskStatus.IN_PROGRESS -> R.string.task_status_in_progress
                    TaskStatus.COMPLETED -> R.string.task_status_completed
                    TaskStatus.OVERDUE -> R.string.task_status_overdue
                }),
                modifier = Modifier.weight(1f),
                isError = displayedStatus == TaskStatus.OVERDUE
            )
        }

        DetailInfoCard(
            label = stringResource(R.string.task_detail_reminder),
            value = if (task.reminderMinutes > 0) {
                stringResource(R.string.task_detail_reminder_before, task.reminderMinutes)
            } else stringResource(R.string.task_detail_no_reminder),
            modifier = Modifier.fillMaxWidth()
        )

        RecurrenceCard(task)
        TaskDetailQuote(taskId = task.id)

        if (actionError != null && actionError != TaskDetailActionError.EDIT) {
            TaskFlowCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(when (actionError) {
                        TaskDetailActionError.DELETE -> R.string.task_detail_delete_error
                        else -> R.string.task_detail_complete_error
                    }),
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = onDismissError) {
                    Text(stringResource(R.string.task_detail_dismiss_error))
                }
            }
        }

        if (isBusy) {
            Text(stringResource(R.string.task_detail_working))
        }
        TaskFlowButton(
            text = stringResource(if (completed) {
                R.string.task_detail_mark_incomplete
            } else R.string.task_detail_mark_complete),
            onClick = onToggleComplete,
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(TaskFlowDimensions.spacing12)) {
            OutlinedButton(
                onClick = onEdit,
                enabled = !isBusy,
                modifier = Modifier.weight(1f).heightIn(min = TaskFlowDimensions.buttonMinHeight),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.task_detail_edit))
            }
            OutlinedButton(
                onClick = onDelete,
                enabled = !isBusy,
                modifier = Modifier.weight(1f).heightIn(min = TaskFlowDimensions.buttonMinHeight),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.task_detail_delete))
            }
        }
    }
}

@Composable
private fun DetailInfoCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    TaskFlowCard(modifier = modifier.heightIn(min = 116.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RecurrenceCard(task: Task) {
    val context = LocalContext.current
    val recurring = task.isRecurring && task.recurrenceType != RecurrenceType.NONE
    TaskFlowCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.task_detail_recurrence),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!recurring) {
            Text(stringResource(R.string.task_detail_no_recurrence))
        } else {
            val interval = task.recurrenceInterval.coerceAtLeast(1)
            Text(
                text = stringResource(when (task.recurrenceType) {
                    RecurrenceType.DAILY -> R.string.task_detail_repeat_days
                    RecurrenceType.WEEKLY -> R.string.task_detail_repeat_weeks
                    RecurrenceType.MONTHLY -> R.string.task_detail_repeat_months
                    RecurrenceType.YEARLY -> R.string.task_detail_repeat_years
                    RecurrenceType.NONE -> R.string.task_detail_no_recurrence
                }, interval),
                style = MaterialTheme.typography.titleMedium
            )
            if (task.isPaused) {
                Text(stringResource(R.string.task_detail_repeat_paused))
            }
            if (task.repeatEndDate > 0L) {
                Text(stringResource(
                    R.string.task_detail_repeat_end_date,
                    DateFormat.getDateFormat(context).format(Date(task.repeatEndDate))
                ))
            }
            if (task.repeatLimitCount > 0) {
                Text(stringResource(
                    R.string.task_detail_repeat_count,
                    task.currentOccurrence,
                    task.repeatLimitCount
                ))
            }
            if (task.repeatEndDate <= 0L && task.repeatLimitCount <= 0) {
                Text(stringResource(R.string.task_detail_repeat_no_end))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, locale = "vi")
@Composable
private fun TaskDetailPreview() {
    TaskFLowTheme {
        TaskDetailContent(
            uiState = TaskDetailUiState(
                task = Task(
                    id = 17,
                    title = "Chuẩn bị bài thuyết trình",
                    description = "Ôn lại nội dung màn hình chi tiết và chuẩn bị demo.",
                    dueDate = DateTimeUtils.getStartOfToday(),
                    dueTime = System.currentTimeMillis() + 3_600_000L,
                    priority = Priority.HIGH,
                    isRecurring = true,
                    recurrenceType = RecurrenceType.WEEKLY,
                    reminderMinutes = 10
                ),
                isLoading = false
            ),
            onBack = {},
            onEdit = {},
            onToggleComplete = {},
            onDelete = {},
            onRetry = {},
            onDismissError = {}
        )
    }
}
