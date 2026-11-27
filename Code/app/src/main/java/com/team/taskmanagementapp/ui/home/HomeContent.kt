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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            val fallbackName = user?.displayName?.takeIf { it.isNotBlank() }
                ?: user?.email?.substringBefore('@')?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                ?: ""
            HomeDashboardSuccessView(
                metrics = HomeSummaryMetrics(),
                streakInfo = HomeStreakInfo(),
                todayTasks = emptyList(),
                upcomingTasks = emptyList(),
                completedTasks = emptyList(),
                userName = fallbackName,
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
                userName = uiState.userName,
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
    userName: String = "",
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

    val greetingText = remember(userName) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val baseGreeting = when (hour) {
            in 4..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }
        if (userName.isNotBlank()) {
            "$baseGreeting, $userName!"
        } else {
            "$baseGreeting!"
        }
    }

    var filterPriority by remember { mutableStateOf<HomeTaskPriority?>(null) }
    var isFilterMenuExpanded by remember { mutableStateOf(false) }

    val displayedTodayTasks = remember(todayTasks, filterPriority) {
        if (filterPriority == null) {
            todayTasks
        } else {
            todayTasks.filter { it.priority == filterPriority }
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
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
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Clock icon in top right (Pomodoro shortcut)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(TaskFlowPrimary.copy(alpha = 0.08f))
                        .border(
                            1.dp,
                            TaskFlowOutlineVariant.copy(alpha = 0.4f),
                            CircleShape
                        )
                        .clickable(onClick = onClockClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = "Pomodoro Timer",
                        tint = TaskFlowPrimary,
                        modifier = Modifier.size(22.dp)
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

                // Filter icon button with priority dropdown menu
                Box {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (filterPriority != null) TaskFlowPrimary.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (filterPriority != null) TaskFlowPrimary
                                else TaskFlowOutlineVariant.copy(alpha = 0.5f),
                                CircleShape
                            )
                            .clickable {
                                onFilterClick()
                                isFilterMenuExpanded = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Filter",
                            tint = if (filterPriority != null) TaskFlowPrimary else TaskFlowOutline,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isFilterMenuExpanded,
                        onDismissRequest = { isFilterMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "All Priorities",
                                    fontWeight = if (filterPriority == null) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                filterPriority = null
                                isFilterMenuExpanded = false
                            },
                            trailingIcon = {
                                if (filterPriority == null) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = TaskFlowPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )

                        HomeTaskPriority.values().forEach { priority ->
                            val (label, dotColor) = when (priority) {
                                HomeTaskPriority.URGENT -> "Urgent" to Color(0xFFE53935)
                                HomeTaskPriority.HIGH -> "High" to Color(0xFFFB8C00)
                                HomeTaskPriority.MEDIUM -> "Medium" to Color(0xFF1E88E5)
                                HomeTaskPriority.LOW -> "Low" to Color(0xFF43A047)
                            }

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                        )
                                        Text(
                                            text = label,
                                            fontWeight = if (filterPriority == priority) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    filterPriority = priority
                                    isFilterMenuExpanded = false
                                },
                                trailingIcon = {
                                    if (filterPriority == priority) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = TaskFlowPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (displayedTodayTasks.isEmpty()) {
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (filterPriority != null) "No tasks with this priority"
                                else stringResource(R.string.home_today_empty),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = TaskFlowOutline
                            ),
                            textAlign = TextAlign.Center
                        )
                        if (filterPriority != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Show all tasks",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TaskFlowPrimary
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { filterPriority = null }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            items(
                items = displayedTodayTasks,
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
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showProgress) {
            Surface(
                shape = CircleShape,
                color = TaskFlowPrimary.copy(alpha = 0.08f),
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = TaskFlowPrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4B5563)
            ),
            textAlign = TextAlign.Center
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            TaskFlowButton(
                text = stringResource(R.string.action_retry),
                onClick = onRetry
            )
        }
    }
}
