package com.team.taskmanagementapp.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.ui.components.TaskFlowConfirmDialog
import com.team.taskmanagementapp.ui.theme.TaskFlowOutline
import com.team.taskmanagementapp.ui.theme.TaskFlowOutlineVariant
import com.team.taskmanagementapp.ui.theme.TaskFlowOverdue
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary
import com.team.taskmanagementapp.ui.theme.TaskFlowSurfaceContainerLowest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeTaskCard(
    task: HomeTaskUiModel,
    onTaskClick: (Long) -> Unit,
    onToggleComplete: (HomeTaskUiModel) -> Unit = {},
    onDeleteTask: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isOverdue = !task.isCompleted && (task.status == HomeTaskStatus.OVERDUE || (task.dueDate > 0L && task.dueDate < System.currentTimeMillis()))

    // Stripe & indicator color
    val indicatorColor = when {
        task.isCompleted -> TaskFlowPrimary
        isOverdue -> Color(0xFFEF4444)
        task.priority == HomeTaskPriority.HIGH || task.priority == HomeTaskPriority.URGENT -> Color(0xFFEF4444)
        task.priority == HomeTaskPriority.MEDIUM -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    // Checkbox border / fill
    val checkboxBorderColor = when {
        task.isCompleted -> TaskFlowPrimary
        isOverdue -> Color(0xFFEF4444)
        else -> Color(0xFF9CA3AF)
    }

    // Priority badge colors
    val (priorityBg, priorityTextColor) = when (task.priority) {
        HomeTaskPriority.LOW -> Color(0xFFD1FAE5) to Color(0xFF059669)
        HomeTaskPriority.MEDIUM -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        HomeTaskPriority.HIGH, HomeTaskPriority.URGENT -> Color(0xFFFEE2E2) to Color(0xFFEF4444)
    }

    val priorityLabel = when (task.priority) {
        HomeTaskPriority.LOW -> "LOW"
        HomeTaskPriority.MEDIUM -> "MEDIUM"
        HomeTaskPriority.HIGH -> "HIGH"
        HomeTaskPriority.URGENT -> "URGENT"
    }

    // Formatted date + time (e.g. "Th9 21, 12:22 PM")
    val timeFormatter = remember {
        SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
    }
    val dateText = if (task.dueDate > 0L) {
        val targetTime = if (task.dueTime != null && task.dueTime > 0L) {
            task.dueTime
        } else {
            task.dueDate
        }
        timeFormatter.format(Date(targetTime))
    } else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button) {
                onTaskClick(task.id)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TaskFlowSurfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                TaskFlowOutlineVariant.copy(alpha = 0.35f)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left colored stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(indicatorColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Circular Checkbox
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (task.isCompleted) TaskFlowPrimary else Color.Transparent)
                        .border(2.dp, checkboxBorderColor, CircleShape)
                        .clickable { onToggleComplete(task) },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.task_completed),
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Middle Content: Title, Description, Chips
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = if (task.isCompleted) Color(0xFF9CA3AF) else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Description / Notes
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color = TaskFlowOutline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Chips Row: Time, Priority, Recurrence
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Time chip
                        if (dateText != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = TaskFlowOutline,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = dateText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = TaskFlowOutline
                                )
                            }
                        }

                        // Priority chip
                        Text(
                            text = priorityLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = priorityTextColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(priorityBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        // Recurrence chip
                        if (task.isRecurring) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF3E8FF))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Repeat,
                                    contentDescription = null,
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = stringResource(R.string.task_detail_recurrence_daily),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    ),
                                    color = Color(0xFF7C3AED)
                                )
                            }
                        }
                    }
                }

                // Top-right: OVERDUE badge & Three dots menu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isOverdue) {
                        Text(
                            text = stringResource(R.string.home_overdue_label),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = Color(0xFFEF4444),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = TaskFlowOutline,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_delete)) },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        TaskFlowConfirmDialog(
            title = stringResource(R.string.task_detail_delete_confirm_title),
            message = stringResource(R.string.task_detail_delete_confirm_msg),
            confirmTextRes = R.string.task_detail_delete_confirm_positive,
            dismissTextRes = R.string.task_detail_delete_confirm_negative,
            onConfirm = {
                showDeleteDialog = false
                onDeleteTask(task.id)
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }
}
