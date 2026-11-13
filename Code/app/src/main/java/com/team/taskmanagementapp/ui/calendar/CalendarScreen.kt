package com.team.taskmanagementapp.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.data.repository.TaskRepository
import com.team.taskmanagementapp.ui.theme.TaskFlowOutline
import com.team.taskmanagementapp.ui.theme.TaskFlowOutlineVariant
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary
import com.team.taskmanagementapp.ui.theme.TaskFlowSurfaceContainerLowest
import androidx.compose.ui.platform.LocalContext

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onTaskClick: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember(context) {
        val database = AppDatabase.getInstance(context)
        TaskRepository(database.taskDao())
    }

    val viewModel = viewModel<CalendarViewModel> {
        CalendarViewModel(repository)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarContent(
        uiState = uiState,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onSelectDate = viewModel::selectDate,
        onTaskClick = onTaskClick,
        modifier = modifier
    )
}

@Composable
fun CalendarContent(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (java.time.LocalDate) -> Unit,
    onTaskClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Top Bar: "Calendar"
        item(key = "calendar_top_bar") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.calendar_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = TaskFlowPrimary
                    )
                }

                HorizontalDivider(
                    color = TaskFlowOutlineVariant.copy(alpha = 0.25f),
                    thickness = 0.5.dp
                )
            }
        }

        // 2. Month Navigation Row (<  tháng 10 2026  >)
        item(key = "month_nav_row") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = Color(0xFF6B7280)
                    )
                }

                Text(
                    text = uiState.monthTitleFormatted,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = Color(0xFF6B7280)
                    )
                }
            }
        }

        // 3. Day of Week Header: S  M  T  W  T  F  S
        item(key = "week_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDays.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Month Grid (7 columns per row)
        val dayRows = uiState.calendarDays.chunked(7)
        items(
            items = dayRows,
            key = { row -> "row_${row.firstOrNull()?.date}_${row.firstOrNull()?.dayOfMonth}" }
        ) { rowDays ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (i in 0 until 7) {
                    val day = rowDays.getOrNull(i)
                    if (day != null && day.isCurrentMonth) {
                        CalendarDayCell(
                            day = day,
                            onClick = { onSelectDate(day.date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Empty placeholder slot for alignment
                        Box(modifier = Modifier.weight(1f).height(44.dp))
                    }
                }
            }
        }

        // 5. Schedule Header: "Schedule  Th10 6"
        item(key = "schedule_header") {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = uiState.scheduleHeaderFormatted,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 6. Schedule Timeline List for Selected Date
        if (uiState.scheduledTasks.isEmpty()) {
            item(key = "schedule_empty") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.calendar_no_tasks_for_date),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            color = TaskFlowOutline
                        )
                    )
                }
            }
        } else {
            items(
                items = uiState.scheduledTasks,
                key = { "task_${it.id}" }
            ) { task ->
                TimelineTaskItem(
                    task = task,
                    onClick = { onTaskClick(task.id) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDayUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(44.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (day.isSelected) TaskFlowPrimary else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (day.isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                ),
                color = if (day.isSelected) Color.White else Color(0xFF1F2937)
            )
        }

        // Orange task bullet indicator dot below date
        if (day.hasTasks) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF59E0B))
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun TimelineTaskItem(
    task: CalendarTaskUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (priorityBg, priorityTextColor) = when (task.priority) {
        "LOW" -> Color(0xFFD1FAE5) to Color(0xFF059669)
        "HIGH", "URGENT" -> Color(0xFFFEE2E2) to Color(0xFFEF4444)
        else -> Color(0xFFFEF3C7) to Color(0xFFD97706)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Time (e.g. "07:38")
        Text(
            text = task.timeDisplay,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF9CA3AF),
            modifier = Modifier.width(42.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Center: Vertical Timeline Line with blue circle dot
        Box(
            modifier = Modifier
                .width(16.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            // Line
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFE5E7EB))
            )

            // Blue Dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(TaskFlowPrimary)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right: Task Card
        Card(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = TaskFlowSurfaceContainerLowest
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    TaskFlowOutlineVariant.copy(alpha = 0.35f)
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Priority Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(priorityBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = task.priorityLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = priorityTextColor
                        )
                    }
                }

                // Time with clock icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = TaskFlowOutline,
                        modifier = Modifier.size(12.dp)
                    )

                    Text(
                        text = task.dueTimeFormatted,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = TaskFlowOutline
                    )
                }
            }
        }
    }
}
