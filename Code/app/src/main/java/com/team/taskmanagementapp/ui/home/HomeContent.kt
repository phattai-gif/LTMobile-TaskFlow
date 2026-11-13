package com.team.taskmanagementapp.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.ui.components.TaskFlowButton
import com.team.taskmanagementapp.ui.theme.TaskFlowDimensions
import com.team.taskmanagementapp.ui.theme.TaskFlowOutline
import com.team.taskmanagementapp.ui.theme.TaskFlowOutlineVariant
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary
import com.team.taskmanagementapp.ui.theme.TaskFlowSurfaceContainerLowest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onTaskClick: (Long) -> Unit,
    onToggleComplete: (HomeTaskUiModel) -> Unit = {},
    onDeleteTask: (Long) -> Unit = {},
    onRetry: () -> Unit = {},
    onViewCalendarClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onClockClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (uiState) {
        HomeUiState.Loading -> {
            HomeStateMessage(
                messageRes = R.string.home_loading,
                modifier = modifier,
                showProgress = true
            )
        }

        HomeUiState.Error -> {
            HomeStateMessage(
                messageRes = R.string.home_load_error,
                modifier = modifier,
                onRetry = onRetry
            )
        }

        HomeUiState.Empty -> {
            HomeDashboardSuccessView(
                metrics = HomeSummaryMetrics(),
                streakInfo = HomeStreakInfo(),
                todayTasks = emptyList(),
                upcomingTasks = emptyList(),
                completedTasks = emptyList(),
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onDeleteTask = onDeleteTask,
                onViewCalendarClick = onViewCalendarClick,
                onFilterClick = onFilterClick,
                onClockClick = onClockClick,
                modifier = modifier
            )
        }

        is HomeUiState.Success -> {
            HomeDashboardSuccessView(
                metrics = uiState.metrics,
                streakInfo = uiState.streakInfo,
                todayTasks = uiState.todayTasks,
                upcomingTasks = uiState.upcomingTasks,
                completedTasks = uiState.completedTasks,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onDeleteTask = onDeleteTask,
                onViewCalendarClick = onViewCalendarClick,
                onFilterClick = onFilterClick,
                onClockClick = onClockClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun HomeDashboardSuccessView(
    metrics: HomeSummaryMetrics,
    streakInfo: HomeStreakInfo,
    todayTasks: List<HomeTaskUiModel>,
    upcomingTasks: List<HomeTaskUiModel>,
    completedTasks: List<HomeTaskUiModel>,
    onTaskClick: (Long) -> Unit,
    onToggleComplete: (HomeTaskUiModel) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onViewCalendarClick: () -> Unit,
    onFilterClick: () -> Unit,
    onClockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateText = remember {
        val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        sdf.format(Date())
    }

    val greetingText = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good Morning!"
            in 12..17 -> "Good Afternoon!"
            else -> "Good Evening!"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header: Date, Greeting, Clock Icon
        item(key = "home_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = TaskFlowOutline
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = greetingText,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onClockClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = stringResource(R.string.pomodoro_entry_content_description),
                        tint = TaskFlowPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 2. 2x2 Metrics Grid
        item(key = "metrics_grid") {
            HomeSummaryMetricsSection(metrics = metrics)
        }

        // 3. Streak Banner Card
        item(key = "streak_card") {
            HomeStreakSection(streakInfo = streakInfo)
        }

        // 4. Section: Today's Tasks
        item(key = "today_tasks_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_today_tasks),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Filter icon button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(1.dp, TaskFlowOutlineVariant.copy(alpha = 0.5f), CircleShape)
                        .clickable(onClick = onFilterClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Filter",
                        tint = TaskFlowOutline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (todayTasks.isEmpty()) {
            item(key = "today_tasks_empty") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = TaskFlowSurfaceContainerLowest
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            TaskFlowOutlineVariant.copy(alpha = 0.35f)
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.home_today_empty),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = TaskFlowOutline
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(
                items = todayTasks,
                key = { "today_${it.id}" }
            ) { task ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    HomeTaskCard(
                        task = task,
                        onTaskClick = onTaskClick,
                        onToggleComplete = onToggleComplete,
                        onDeleteTask = onDeleteTask
                    )
                }
            }
        }

        // 5. Section: Upcoming
        item(key = "upcoming_header") {
            Text(
                text = stringResource(R.string.home_upcoming_tasks),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (upcomingTasks.isEmpty()) {
            item(key = "upcoming_empty_card") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = TaskFlowSurfaceContainerLowest
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            TaskFlowOutlineVariant.copy(alpha = 0.35f)
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.home_upcoming_empty),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = TaskFlowOutline
                            ),
                            textAlign = TextAlign.Center
                        )

                        OutlinedButton(
                            onClick = onViewCalendarClick,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TaskFlowPrimary
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TaskFlowPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.home_view_full_calendar),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        } else {
            items(
                items = upcomingTasks,
                key = { "upcoming_${it.id}" }
            ) { task ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    HomeTaskCard(
                        task = task,
                        onTaskClick = onTaskClick,
                        onToggleComplete = onToggleComplete,
                        onDeleteTask = onDeleteTask
                    )
                }
            }
        }

        // 6. Section: Completed
        item(key = "completed_header") {
            Text(
                text = stringResource(R.string.home_completed_tasks),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (completedTasks.isEmpty()) {
            item(key = "completed_empty") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = TaskFlowSurfaceContainerLowest
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            TaskFlowOutlineVariant.copy(alpha = 0.35f)
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.home_completed_empty),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = TaskFlowOutline
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(
                items = completedTasks,
                key = { "completed_${it.id}" }
            ) { task ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    HomeTaskCard(
                        task = task,
                        onTaskClick = onTaskClick,
                        onToggleComplete = onToggleComplete,
                        onDeleteTask = onDeleteTask
                    )
                }
            }
        }

        // 7. Section: Productivity tip Banner
        item(key = "productivity_tip_banner") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0D50C6)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.home_productivity_tip_title),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color.White
                            )
                        }

                        // Circular + button on right
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2563EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add tip",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.home_productivity_tip_body),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeStateMessage(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(TaskFlowDimensions.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            TaskFlowDimensions.spacing16,
            Alignment.CenterVertically
        )
    ) {
        if (showProgress) {
            CircularProgressIndicator(color = TaskFlowPrimary)
        }

        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        if (onRetry != null) {
            TaskFlowButton(
                text = stringResource(R.string.action_retry),
                onClick = onRetry
            )
        }
    }
}
