package com.team.taskmanagementapp.ui.pomodoro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.SessionType
import com.team.taskmanagementapp.pomodoro.PomodoroSnapshot
import com.team.taskmanagementapp.pomodoro.PomodoroTimerState
import com.team.taskmanagementapp.pomodoro.pomodoroLabel
import com.team.taskmanagementapp.ui.theme.TaskFlowSuccess
import com.team.taskmanagementapp.ui.theme.TaskFlowWarning
import com.team.taskmanagementapp.ui.viewmodel.PomodoroViewModel
import com.team.taskmanagementapp.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PomodoroScreen(
    viewModel: PomodoroViewModel,
    onBack: () -> Unit,
    onSettings: () -> Unit
) {
    val snapshot by viewModel.timer.collectAsStateWithLifecycle()
    val selectedTask by viewModel.selectedTask.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var showTaskPicker by rememberSaveable { mutableStateOf(false) }
    val requestNotifications = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (!viewModel.onPrimaryAction()) showTaskPicker = true
    }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(context.getString(it))
            viewModel.dismissMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            PomodoroTopBar(
                title = stringResource(R.string.pomodoro_title),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.pomodoro_settings_title)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TimerDisplay(snapshot)
            val canSelectTask = snapshot.state == PomodoroTimerState.IDLE
            val task = selectedTask
            Card(
                onClick = { showTaskPicker = true },
                enabled = canSelectTask,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        stringResource(
                            if (canSelectTask) R.string.pomodoro_task_label
                            else R.string.pomodoro_task_label_active
                        ),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        task?.title ?: stringResource(
                            if (snapshot.taskId != null) R.string.pomodoro_task_removed
                            else R.string.pomodoro_task_none
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    val detail = when {
                        task != null -> taskDueLabel(task)
                        canSelectTask -> stringResource(R.string.pomodoro_task_none_hint)
                        else -> stringResource(R.string.pomodoro_task_locked_hint)
                    }
                    Text(detail, style = MaterialTheme.typography.bodySmall)
                }
            }
            TimerControls(
                snapshot = snapshot,
                onPrimary = {
                    if (snapshot.state == PomodoroTimerState.IDLE &&
                        selectedTask != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                    ) {
                        requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else if (!viewModel.onPrimaryAction()) {
                        showTaskPicker = true
                    }
                },
                onPause = viewModel::pause,
                onSkip = viewModel::skip,
                onStop = viewModel::stop
            )
        }
    }

    if (showTaskPicker && snapshot.state == PomodoroTimerState.IDLE) {
        TaskPicker(
            tasks = tasks,
            selectedTaskId = selectedTask?.id,
            onSelect = {
                viewModel.selectTask(it)
                showTaskPicker = false
            },
            onDismiss = { showTaskPicker = false }
        )
    }
}

@Composable
private fun TimerDisplay(snapshot: PomodoroSnapshot) {
    val accent = when (snapshot.sessionType) {
        SessionType.FOCUS -> MaterialTheme.colorScheme.primary
        SessionType.SHORT_BREAK -> TaskFlowSuccess
        SessionType.LONG_BREAK -> TaskFlowWarning
    }
    val progressDescription = stringResource(R.string.pomodoro_progress_description)
    Text(
        stringResource(snapshot.sessionType.pomodoroLabel),
        color = accent,
        style = MaterialTheme.typography.titleLarge
    )
    Text(
        stringResource(
            R.string.pomodoro_cycle_label, snapshot.currentCycle.coerceAtLeast(1), snapshot.totalCycles
        ),
        style = MaterialTheme.typography.bodyMedium
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(216.dp)) {
        CircularProgressIndicator(
            progress = { 1f - snapshot.progressFraction },
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = progressDescription },
            color = accent,
            strokeWidth = 10.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                snapshot.formattedDisplay,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(
                    if (snapshot.state == PomodoroTimerState.COMPLETED &&
                        snapshot.lastCompletedSession?.isCompleted == false
                    ) R.string.pomodoro_status_skipped else snapshot.state.pomodoroLabel
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            if (snapshot.state == PomodoroTimerState.COMPLETED) {
                Text(
                    stringResource(
                        R.string.pomodoro_next_session,
                        stringResource(snapshot.nextSessionType.pomodoroLabel)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(snapshot.totalCycles) { index ->
            Box(
                Modifier.size(8.dp).background(
                    color = if (index < snapshot.focusSessionsInCurrentSet) accent
                    else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                )
            )
        }
    }
    Text(
        stringResource(R.string.pomodoro_completed_focus_count, snapshot.completedFocusSessions),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun TimerControls(
    snapshot: PomodoroSnapshot,
    onPrimary: () -> Unit,
    onPause: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit
) {
    val primaryLabel = when (snapshot.state) {
        PomodoroTimerState.IDLE -> R.string.pomodoro_action_start
        PomodoroTimerState.RUNNING -> R.string.pomodoro_status_running
        PomodoroTimerState.PAUSED -> R.string.pomodoro_action_resume
        PomodoroTimerState.COMPLETED -> R.string.pomodoro_action_start_next
    }
    Button(
        onClick = onPrimary,
        enabled = snapshot.state != PomodoroTimerState.RUNNING,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(primaryLabel))
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onPause,
            enabled = snapshot.state == PomodoroTimerState.RUNNING,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.pomodoro_action_pause), textAlign = TextAlign.Center)
        }
        OutlinedButton(
            onClick = onSkip,
            enabled = snapshot.isActive,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.pomodoro_action_skip))
        }
        OutlinedButton(
            onClick = onStop,
            enabled = snapshot.state != PomodoroTimerState.IDLE,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.pomodoro_action_stop))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskPicker(
    tasks: List<Task>,
    selectedTaskId: Int?,
    onSelect: (Task) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            stringResource(R.string.pomodoro_task_selector_title),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge
        )
        if (tasks.isEmpty()) {
            Text(
                stringResource(R.string.pomodoro_task_selector_empty),
                modifier = Modifier.padding(20.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                items(tasks, key = { it.id }) { task ->
                    Surface(onClick = { onSelect(task) }) {
                        ListItem(
                            headlineContent = {
                                Text(task.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            },
                            supportingContent = { Text(taskDueLabel(task)) },
                            trailingContent = if (task.id == selectedTaskId) {
                                { Icon(Icons.Filled.Check, contentDescription = null) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun taskDueLabel(task: Task): String {
    val dueDate = DateTimeUtils.formatTimestamp(
        task.dueDate.takeIf { it > 0L }, DateTimeUtils.FORMAT_DATE_ONLY
    )
    val label = if (dueDate.isEmpty()) {
        stringResource(R.string.pomodoro_task_no_due_date)
    } else {
        stringResource(R.string.pomodoro_task_due_format, dueDate)
    }
    return if (task.isCompleted) {
        stringResource(R.string.pomodoro_task_due_completed, label)
    } else {
        label
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PomodoroTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back)
                )
            }
        },
        actions = actions
    )
}
