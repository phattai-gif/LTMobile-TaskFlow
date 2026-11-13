package com.team.taskmanagementapp.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.data.model.stats.StatsTimeFilter
import com.team.taskmanagementapp.data.model.stats.formatFocusDuration
import com.team.taskmanagementapp.data.repository.TaskRepository
import com.team.taskmanagementapp.ui.theme.TaskFlowOutline
import com.team.taskmanagementapp.ui.theme.TaskFlowOutlineVariant
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary
import com.team.taskmanagementapp.ui.theme.TaskFlowSurfaceContainerLowest
import kotlin.math.roundToInt

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val database = remember(context) { AppDatabase.getInstance(context) }
    val repository = remember(database) { TaskRepository(database.taskDao()) }
    val pomodoroDao = remember(database) { database.pomodoroDao() }

    val viewModel = viewModel<StatsViewModel> {
        StatsViewModel(repository, pomodoroDao, createSavedStateHandle())
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatsContent(
        uiState = uiState,
        onSelectPeriod = viewModel::selectPeriod,
        onRetry = viewModel::retry,
        modifier = modifier
    )
}

@Composable
internal fun StatsContent(
    uiState: StatsUiState,
    onSelectPeriod: (StatsTimeFilter) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val periodLabel = stringResource(uiState.timeFilter.labelRes)
    var showFilters by rememberSaveable { mutableStateOf(false) }
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

                Box {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = stringResource(R.string.stats_filter_description, periodLabel),
                            tint = TaskFlowOutline
                        )
                    }
                    DropdownMenu(expanded = showFilters, onDismissRequest = { showFilters = false }) {
                        StatsTimeFilter.entries.forEach { filter ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(filter.labelRes),
                                        fontWeight = if (uiState.timeFilter == filter) FontWeight.Bold
                                        else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    showFilters = false
                                    onSelectPeriod(filter)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isLoading || uiState.hasError || uiState.isEmpty) {
            item(key = "stats_message") {
                StatsMessage(uiState, periodLabel, onRetry)
            }
            if (uiState.isEmpty && uiState.hasUnknownCompletionDates) {
                item(key = "stats_unknown_completion_dates") {
                    Text(
                        text = stringResource(R.string.stats_unknown_completion_dates),
                        style = MaterialTheme.typography.bodySmall,
                        color = TaskFlowOutline
                    )
                }
            }
            return@LazyColumn
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
                        text = stringResource(R.string.stats_chart_period, periodLabel),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = TaskFlowOutline
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (uiState.weekdayCompletions.any { it.count > 0 }) {
                        WeekdayBarChart(
                            weekdayCompletions = uiState.weekdayCompletions,
                            maxCount = uiState.maxWeekdayCount,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.stats_no_dated_completions),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TaskFlowOutline
                        )
                    }
                    if (uiState.hasUnknownCompletionDates) {
                        Text(
                            text = stringResource(R.string.stats_unknown_completion_dates),
                            modifier = Modifier.padding(top = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskFlowOutline
                        )
                    }
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

                            CircularProgressIndicator(
                                progress = { progressFloat },
                                modifier = Modifier.fillMaxSize(),
                                color = TaskFlowPrimary,
                                trackColor = Color(0xFFE5E7EB),
                                strokeWidth = 9.dp
                            )

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
                                    text = stringResource(uiState.completionLabelRes),
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
                                text = uiState.completedCount.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                ),
                                color = Color.White
                            )

                            Text(
                                text = periodLabel,
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
                                text = uiState.pendingCount.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                ),
                                color = Color.White
                            )

                            Text(
                                text = periodLabel,
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

                    if (dist.totalCount == 0) {
                        Text(
                            text = stringResource(R.string.stats_no_tasks),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TaskFlowOutline
                        )
                    } else {
                        PriorityProgressRow(
                            title = stringResource(R.string.stats_urgent_priority),
                            count = dist.urgentCount,
                            progress = dist.urgentCount.toFloat() / maxPriority,
                            barColor = Color(0xFFBA1A1A)
                        )

                        PriorityProgressRow(
                            title = stringResource(R.string.stats_high_priority),
                            count = dist.highCount,
                            progress = dist.highCount.toFloat() / maxPriority,
                            barColor = Color(0xFFEF4444)
                        )

                        PriorityProgressRow(
                            title = stringResource(R.string.stats_medium_priority),
                            count = dist.mediumCount,
                            progress = dist.mediumCount.toFloat() / maxPriority,
                            barColor = Color(0xFFF59E0B)
                        )

                        PriorityProgressRow(
                            title = stringResource(R.string.stats_low_priority),
                            count = dist.lowCount,
                            progress = dist.lowCount.toFloat() / maxPriority,
                            barColor = Color(0xFF10B981)
                        )
                    }
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
                                text = formatFocusDuration(uiState.pomodoroStats.todayMinutes),
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
                                text = formatFocusDuration(uiState.pomodoroStats.thisWeekMinutes),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val pomodoro = uiState.pomodoroStats
                    val focusDetail = if (pomodoro.periodSessionCount > 0) {
                        val sessions = pluralStringResource(
                            R.plurals.stats_pomodoro_completed,
                            pomodoro.periodSessionCount,
                            pomodoro.periodSessionCount
                        )
                        stringResource(
                            R.string.stats_focus_period_summary,
                            periodLabel,
                            formatFocusDuration(pomodoro.periodMinutes),
                            sessions
                        )
                    } else if (pomodoro.hasFocus) {
                        stringResource(R.string.stats_no_focus_in_period, periodLabel)
                    } else {
                        stringResource(R.string.stats_no_focus_sessions)
                    }
                    Text(
                        text = focusDetail,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = TaskFlowOutline
                        )
                    )
                    if (pomodoro.topTasks.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.stats_top_focus_tasks),
                            style = MaterialTheme.typography.titleSmall
                        )
                        pomodoro.topTasks.forEach { task ->
                            val sessions = pluralStringResource(
                                R.plurals.stats_session_count, task.sessionCount, task.sessionCount
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = task.taskTitle,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = stringResource(
                                        R.string.stats_focus_task_summary,
                                        formatFocusDuration(task.totalMinutes),
                                        sessions
                                    ),
                                    color = TaskFlowOutline,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val StatsTimeFilter.labelRes: Int
    get() = when (this) {
        StatsTimeFilter.THIS_WEEK -> R.string.stats_this_week
        StatsTimeFilter.LAST_WEEK -> R.string.stats_last_week
        StatsTimeFilter.THIS_MONTH -> R.string.stats_this_month
        StatsTimeFilter.ALL_TIME -> R.string.stats_all_time
    }

private val StatsUiState.completionLabelRes: Int
    get() = when {
        priorityDistribution.totalCount == 0 -> R.string.stats_no_tasks
        completionRatePercent >= 85 -> R.string.stats_excellent
        completionRatePercent >= 65 -> R.string.stats_on_track
        completionRatePercent >= 40 -> R.string.stats_in_progress
        else -> R.string.stats_needs_attention
    }

@Composable
private fun StatsMessage(uiState: StatsUiState, periodLabel: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        if (uiState.isLoading) CircularProgressIndicator()
        Text(
            text = when {
                uiState.isLoading -> stringResource(R.string.stats_loading)
                uiState.hasError -> stringResource(R.string.stats_load_error)
                else -> stringResource(R.string.stats_empty_period, periodLabel)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TaskFlowOutline,
            textAlign = TextAlign.Center
        )
        if (uiState.hasError) {
            TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
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
                text = pluralStringResource(R.plurals.stats_task_count, count, count),
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
    val chartDescription = weekdayCompletions.joinToString(", ") { "${it.dayLabel} ${it.count}" }
    val step = maxCount / 3
    val axisStyle = MaterialTheme.typography.labelSmall
    Column(modifier = modifier.semantics { contentDescription = chartDescription }) {
        Row(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .width(32.dp)
                    .fillMaxHeight()
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                for (level in 3 downTo 0) {
                    Text((level * step).toString(), style = axisStyle, color = TaskFlowOutline)
                }
            }
            Box(Modifier.fillMaxHeight().weight(1f)) {
                Canvas(Modifier.fillMaxSize()) {
                    for (line in 0..3) {
                        val y = size.height * line / 3f
                        drawLine(Color(0xFFE5E7EB), Offset(0f, y), Offset(size.width, y))
                    }
                }
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Bottom) {
                    weekdayCompletions.forEach { item ->
                        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                            if (item.count > 0) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(0.45f)
                                        .fillMaxHeight((item.count.toFloat() / maxCount).coerceIn(0f, 1f))
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(TaskFlowPrimary)
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 32.dp, top = 6.dp)) {
            weekdayCompletions.forEach { item ->
                Text(
                    text = item.dayLabel,
                    modifier = Modifier.weight(1f),
                    style = axisStyle,
                    color = TaskFlowOutline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
