package com.example.taskflow.ui.home

import com.example.taskflow.ui.home.HomeTaskCard
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.team.taskmanagementapp.R
import com.example.taskflow.ui.components.TaskFlowCard
import com.example.taskflow.ui.theme.TaskFlowDimensions
import java.util.Date

@Composable
fun HomeTaskCard(
    task: HomeTaskUiModel,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val dateText = if (task.dueDate > 0L) {
        DateFormat.getDateFormat(context).format(Date(task.dueDate))
    } else {
        stringResource(R.string.home_no_date)
    }

    val dueTime = task.dueTime
    val timeText = if (dueTime != null && dueTime > 0L) {
        DateFormat.getTimeFormat(context).format(Date(dueTime))
    } else {
        stringResource(R.string.home_no_time)
    }

    val priorityText = stringResource(
        when (task.priority) {
            HomeTaskPriority.LOW -> R.string.task_priority_low
            HomeTaskPriority.MEDIUM -> R.string.task_priority_medium
            HomeTaskPriority.HIGH -> R.string.task_priority_high
            HomeTaskPriority.URGENT -> R.string.task_priority_urgent
        }
    )

    val statusText = stringResource(
        when (task.status) {
            HomeTaskStatus.PENDING -> R.string.task_status_pending
            HomeTaskStatus.IN_PROGRESS -> R.string.task_status_in_progress
            HomeTaskStatus.COMPLETED -> R.string.task_status_completed
            HomeTaskStatus.OVERDUE -> R.string.task_status_overdue
        }
    )

    val statusColor = when (task.status) {
        HomeTaskStatus.OVERDUE -> MaterialTheme.colorScheme.error
        HomeTaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    TaskFlowCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(role = Role.Button) {
                onTaskClick(task.id)
            }
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(
                TaskFlowDimensions.spacing4
            )
        ) {
            Text(
                text = stringResource(
                    R.string.home_task_due,
                    dateText,
                    timeText
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(
                    R.string.home_task_priority,
                    priorityText
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(
                    R.string.home_task_status,
                    statusText
                ),
                style = MaterialTheme.typography.bodySmall,
                color = statusColor
            )
        }
    }
}