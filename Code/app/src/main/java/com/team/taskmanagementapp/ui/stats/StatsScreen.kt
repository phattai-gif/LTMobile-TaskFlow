package com.team.taskmanagementapp.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
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
import com.team.taskmanagementapp.ui.theme.TaskFlowSurfaceContainer
import com.team.taskmanagementapp.ui.theme.TaskFlowSurfaceContainerLowest

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    onFilterClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember(context) { AppDatabase.getInstance(context) }
    val repository = remember(database) { TaskRepository(database.taskDao()) }
    val pomodoroDao = remember(database) { database.pomodoroDao() }

    val viewModel = viewModel<StatsViewModel> {
        StatsViewModel(repository, pomodoroDao)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatsContent(
        uiState = uiState,
        onFilterClick = onFilterClick,
        modifier = modifier
    )
}

@Composable
fun StatsContent(
    uiState: StatsUiState,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Bar: "Statistics" + Filter Icon
        item(key = "stats_top_bar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.stats_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                        .clickable(onClick = onFilterClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Filter",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Card 1: Completions by weekday (Chart)
        item(key = "completions_by_weekday") {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.stats_completions_by_weekday),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = stringResource(R.string.stats_completions_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = TaskFlowOutline
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bar Chart
                    WeekdayBarChart(
                        weekdayCompletions = uiState.weekdayCompletions,
                        maxCount = uiState.maxWeekdayCount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            }
        }

        // 3. Section 2: Completion Rate (Left) & Completed / Not Completed (Right)
        item(key = "completion_rate_and_counts") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Column: Completion Rate
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.stats_completion_rate),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = Color(0xFF4B5563)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Radial Progress Circle
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val progressFloat = uiState.completionRatePercent / 100f

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 9.dp.toPx()
                                // Background circle
                                drawCircle(
                                    color = Color(0xFFE5E7EB),
                                    style = Stroke(width = strokeWidth)
                                )
                                // Active arc
                                drawArc(
                                    color = TaskFlowPrimary,
                                    startAngle = -90f,
                                    sweepAngle = progressFloat * 360f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Text(
                                text = "${uiState.completionRatePercent}%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = TaskFlowPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = uiState.completionRateStatus,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = TaskFlowOutline
                            )
                        )
                    }
                }

                // Right Column: Completed (Top) & Not Completed (Bottom)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // COMPLETED CARD (Deep Blue)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0D50C6)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.stats_completed_label),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Text(
                                text = uiState.completedThisWeek.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                ),
                                color = Color.White
                            )

                            Text(
                                text = stringResource(R.string.stats_this_week),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp
                                ),
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }

                    // NOT COMPLETED CARD (Lighter Blue)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1D4ED8)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.stats_not_completed_label),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Text(
                                text = uiState.notCompletedThisWeek.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                ),
                                color = Color.White
                            )

                            Text(
                                text = stringResource(R.string.stats_this_week),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp
                                ),
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
        }

        // 4. Card 3: Tasks by Priority
        item(key = "tasks_by_priority") {
            val dist = uiState.priorityDistribution
            val maxPriority = maxOf(1, dist.totalCount)

            Card(
                modifier = Modifier.fillMaxWidth(),
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.stats_tasks_by_priority),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Urgent Priority
                    PriorityProgressRow(
                        title = stringResource(R.string.stats_urgent_priority),
                        count = dist.urgentCount,
                        progress = dist.urgentCount.toFloat() / maxPriority,
                        barColor = Color(0xFFBA1A1A)
                    )

                    // High Priority
                    PriorityProgressRow(
                        title = stringResource(R.string.stats_high_priority),
                        count = dist.highCount,
                        progress = dist.highCount.toFloat() / maxPriority,
                        barColor = Color(0xFFEF4444)
                    )

                    // Medium Priority
                    PriorityProgressRow(
                        title = stringResource(R.string.stats_medium_priority),
                        count = dist.mediumCount,
                        progress = dist.mediumCount.toFloat() / maxPriority,
                        barColor = Color(0xFFF59E0B)
                    )

                    // Low Priority
                    PriorityProgressRow(
                        title = stringResource(R.string.stats_low_priority),
                        count = dist.lowCount,
                        progress = dist.lowCount.toFloat() / maxPriority,
                        barColor = Color(0xFF10B981)
                    )
                }
            }
        }

        // 5. Card 4: Pomodoro Focus
        item(key = "pomodoro_focus") {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.stats_pomodoro_focus),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = stringResource(R.string.stats_pomodoro_focus_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = TaskFlowOutline
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        // TODAY
                        Column {
                            Text(
                                text = stringResource(R.string.stats_today),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TaskFlowOutline
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.pomodoroStats.todayFormatted,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // THIS WEEK
                        Column {
                            Text(
                                text = stringResource(R.string.stats_this_week).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TaskFlowOutline
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.pomodoroStats.thisWeekFormatted,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.stats_no_focus_sessions),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = TaskFlowOutline
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityProgressRow(
    title: String,
    count: Int,
    progress: Float,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.stats_tasks_count, count),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = TaskFlowOutline
                )
            )
        }

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = Color(0xFFEEEEEE)
        )
    }
}

@Composable
private fun WeekdayBarChart(
    weekdayCompletions: List<WeekdayCompletion>,
    maxCount: Int,
    modifier: Modifier = Modifier
) {
    val yLevels = listOf(12, 8, 4, 0)

    Row(modifier = modifier) {
        // Y-axis labels
        Column(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            yLevels.forEach { level ->
                Text(
                    text = level.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = Color(0xFF9CA3AF)
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp)) // padding for X-axis labels
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Chart canvas & X-axis
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Bars & Grid Lines
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Horizontal grid lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineSpacing = size.height / 3f
                    for (i in 0..3) {
                        val y = i * lineSpacing
                        drawLine(
                            color = Color(0xFFE5E7EB),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // Bars
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weekdayCompletions.forEach { item ->
                        val barHeightFactor = if (maxCount > 0) {
                            (item.count.toFloat() / maxCount).coerceIn(0f, 1f)
                        } else 0f

                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (item.count > 0) {
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .fillMaxHeight(barHeightFactor)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(TaskFlowPrimary)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-axis day labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekdayCompletions.forEach { item ->
                    Text(
                        text = item.dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = Color(0xFF9CA3AF)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
