package com.team.taskmanagementapp.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team.taskmanagementapp.ui.components.TaskFlowButton
import com.team.taskmanagementapp.ui.components.TaskFlowCard
import com.team.taskmanagementapp.ui.home.HomeTaskCard
import com.team.taskmanagementapp.ui.home.HomeTaskUiModel
import com.team.taskmanagementapp.ui.theme.TaskFLowTheme
import com.team.taskmanagementapp.ui.theme.TaskFlowDimensions
import com.team.taskmanagementapp.R
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CalendarContent(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTaskClick: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = TaskFlowDimensions.screenPadding,
            end = TaskFlowDimensions.screenPadding,
            top = TaskFlowDimensions.screenPadding,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(TaskFlowDimensions.spacing16)
    ) {
        item {
            Text(
                text = stringResource(R.string.calendar_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            MonthCalendarCard(
                uiState = uiState,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onToday = onToday,
                onDateSelected = onDateSelected
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(TaskFlowDimensions.spacing4)) {
                Text(
                    text = stringResource(
                        R.string.calendar_tasks_for_date,
                        uiState.selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    ),
                    style = MaterialTheme.typography.titleLarge
                )
                if (!uiState.isLoading && !uiState.hasError) {
                    Text(
                        text = stringResource(
                            R.string.calendar_task_count,
                            uiState.tasksForSelectedDate.size
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        when {
            uiState.isLoading -> item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TaskFlowDimensions.spacing12)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(stringResource(R.string.calendar_loading))
                }
            }
            uiState.hasError -> item {
                TaskFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.calendar_load_error),
                        color = MaterialTheme.colorScheme.error
                    )
                    TaskFlowButton(
                        text = stringResource(R.string.action_retry),
                        onClick = onRetry
                    )
                }
            }
            uiState.tasksForSelectedDate.isEmpty() -> item {
                TaskFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.calendar_empty_day),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> items(uiState.tasksForSelectedDate, key = { it.id }) { task ->
                HomeTaskCard(task = task, onTaskClick = onTaskClick)
            }
        }
    }
}

@Composable
private fun MonthCalendarCard(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val weekdays = stringArrayResource(R.array.calendar_weekdays)
    val previousLabel = stringResource(R.string.calendar_previous_month)
    val nextLabel = stringResource(R.string.calendar_next_month)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column(modifier = Modifier.padding(TaskFlowDimensions.spacing12)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier.semantics { contentDescription = previousLabel }
                ) {
                    Text(
                        text = "‹",
                        modifier = Modifier.clearAndSetSemantics {},
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Text(
                    text = stringResource(
                        R.string.calendar_month_year,
                        uiState.displayedMonth.monthValue,
                        uiState.displayedMonth.year
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.semantics { contentDescription = nextLabel }
                ) {
                    Text(
                        text = "›",
                        modifier = Modifier.clearAndSetSemantics {},
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                weekdays.forEach { weekday ->
                    Text(
                        text = weekday,
                        modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            uiState.days.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        CalendarDayCell(
                            day = day,
                            onDateSelected = onDateSelected,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onToday) {
                    Text(stringResource(R.string.calendar_today))
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDayUiModel,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val date = day.date
    if (date == null) {
        Spacer(modifier = modifier.height(52.dp))
        return
    }
    val shape = RoundedCornerShape(12.dp)
    val label = stringResource(
        R.string.calendar_day_description,
        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
        day.taskCount
    )
    val contentColor = if (day.isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .height(52.dp)
            .padding(2.dp)
            .clip(shape)
            .background(if (day.isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (day.isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = shape
            )
            .selectable(
                selected = day.isSelected,
                role = Role.Button,
                onClick = { onDateSelected(date) }
            )
            .semantics(mergeDescendants = true) { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            modifier = Modifier.clearAndSetSemantics {},
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier.size(5.dp).background(
                color = when {
                    !day.hasTasks -> Color.Transparent
                    day.isSelected -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.primary
                },
                shape = CircleShape
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 850, locale = "vi")
@Composable
private fun CalendarPreview() {
    val date = LocalDate.of(2026, 11, 6)
    val zone = ZoneId.systemDefault()
    val task = HomeTaskUiModel(
        id = 24,
        title = "Chuẩn bị bài thuyết trình",
        description = "Kiểm tra lịch công việc",
        dueDate = date.atStartOfDay(zone).toInstant().toEpochMilli(),
        dueTime = date.atTime(9, 30).atZone(zone).toInstant().toEpochMilli()
    )
    TaskFLowTheme {
        CalendarContent(
            uiState = CalendarDateHelper.buildUiState(date, listOf(task), zone, date),
            onPreviousMonth = {},
            onNextMonth = {},
            onToday = {},
            onDateSelected = {},
            onTaskClick = {},
            onRetry = {}
        )
    }
}
