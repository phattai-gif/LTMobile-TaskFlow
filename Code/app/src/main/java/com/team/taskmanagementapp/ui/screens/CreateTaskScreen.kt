package com.team.taskmanagementapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.RecurrenceType
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.data.repository.TaskRepository
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CreateTaskScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val repository = remember(context) {
        val database = AppDatabase.getInstance(context)
        TaskRepository(database.taskDao())
    }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    val initialCalendar = remember {
        Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    var selectedDueDate by rememberSaveable {
        mutableLongStateOf(initialCalendar.timeInMillis)
    }

    var selectedDueTime by rememberSaveable {
        mutableLongStateOf(initialCalendar.timeInMillis)
    }

    var selectedPriority by rememberSaveable {
        mutableStateOf(Priority.MEDIUM)
    }

    var selectedStatus by rememberSaveable {
        mutableStateOf(TaskStatus.TODO)
    }

    var selectedReminderMinutes by rememberSaveable {
        mutableIntStateOf(30)
    }

    var selectedRecurrence by rememberSaveable {
        mutableStateOf(RecurrenceType.NONE)
    }

    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var reminderDropdownExpanded by remember { mutableStateOf(false) }

    fun formatDate(millis: Long): String {
        val locale = Locale.getDefault()
        val sdf = SimpleDateFormat("MMM dd, yyyy", locale)
        val formatted = sdf.format(Date(millis))
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        return sdf.format(Date(millis))
    }

    fun reminderLabel(minutes: Int): String {
        return when (minutes) {
            0 -> "None"
            10 -> "10 mins before"
            30 -> "30 mins before"
            60 -> "1 hour before"
            1440 -> "1 day before"
            else -> "$minutes mins before"
        }
    }

    val emptyTitleError = stringResource(R.string.create_task_empty_title_error)

    fun onSave() {
        focusManager.clearFocus()
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(emptyTitleError)
            }
            return
        }

        coroutineScope.launch {
            try {
                val now = System.currentTimeMillis()
                val newTask = Task(
                    title = trimmedTitle,
                    description = description.trim(),
                    dueDate = selectedDueDate,
                    dueTime = selectedDueTime,
                    priority = selectedPriority,
                    status = selectedStatus,
                    isCompleted = selectedStatus == TaskStatus.COMPLETED,
                    isRecurring = selectedRecurrence != RecurrenceType.NONE,
                    recurrenceType = selectedRecurrence,
                    reminderMinutes = selectedReminderMinutes,
                    createdAt = now,
                    updatedAt = now,
                    completedAt = if (selectedStatus == TaskStatus.COMPLETED) now else null
                )
                repository.insert(newTask)
                onBack()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    e.localizedMessage ?: "Failed to save task"
                )
            }
        }
    }

    fun showDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDueDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDueDate = newCal.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showTimePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDueTime }
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val newCal = Calendar.getInstance().apply {
                    timeInMillis = selectedDueDate
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDueTime = newCal.timeInMillis
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFAFAFC),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TaskFlowPrimary
                    )
                }
                Text(
                    text = stringResource(R.string.create_task_screen_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TaskFlowPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )
                IconButton(onClick = { onSave() }) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .border(2.dp, TaskFlowPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Task",
                            tint = TaskFlowPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Card 1: TASK TITLE ──────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.create_task_title_label_upper),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp)
                    ) {
                        if (title.isEmpty()) {
                            Text(
                                text = stringResource(R.string.create_task_title_hint),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F2937)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── Card 2: DESCRIPTION ─────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.create_task_desc_label_upper),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .padding(top = 16.dp)
                    ) {
                        if (description.isEmpty()) {
                            Text(
                                text = stringResource(R.string.create_task_desc_hint),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        BasicTextField(
                            value = description,
                            onValueChange = { description = it },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF1F2937)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── Card 3: SCHEDULE ────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.create_task_schedule_label),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Date Pill
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showDatePicker() },
                        color = Color(0xFFEEF4FF),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = TaskFlowPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = formatDate(selectedDueDate),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F2937),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Time Pill
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showTimePicker() },
                        color = Color(0xFFEEF4FF),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = TaskFlowPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = formatTime(selectedDueTime),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F2937),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // ── Card 4: PRIORITY ────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.create_task_priority_label),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            Priority.LOW to "Low",
                            Priority.MEDIUM to "Medium",
                            Priority.HIGH to "High"
                        ).forEach { (p, label) ->
                            val isSelected = selectedPriority == p
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable { selectedPriority = p },
                                shape = RoundedCornerShape(22.dp),
                                color = if (isSelected) TaskFlowPrimary else Color.Transparent,
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF374151)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Card 5: INITIAL STATUS, REMINDER, RECURRENCE ────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left: Initial Status
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.create_task_initial_status_label),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { statusDropdownExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF3F4F6),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (selectedStatus == TaskStatus.IN_PROGRESS) {
                                                stringResource(R.string.create_task_status_in_progress)
                                            } else {
                                                stringResource(R.string.create_task_status_todo)
                                            },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF1F2937),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.ChevronRight,
                                            contentDescription = null,
                                            tint = Color(0xFF6B7280),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = statusDropdownExpanded,
                                    onDismissRequest = { statusDropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.create_task_status_todo)) },
                                        onClick = {
                                            selectedStatus = TaskStatus.TODO
                                            statusDropdownExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.create_task_status_in_progress)) },
                                        onClick = {
                                            selectedStatus = TaskStatus.IN_PROGRESS
                                            statusDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Right: Reminder
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.create_task_reminder_label),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { reminderDropdownExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF3F4F6),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = reminderLabel(selectedReminderMinutes),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF1F2937),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Filled.Notifications,
                                            contentDescription = null,
                                            tint = TaskFlowPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = reminderDropdownExpanded,
                                    onDismissRequest = { reminderDropdownExpanded = false }
                                ) {
                                    listOf(
                                        0 to stringResource(R.string.create_task_reminder_none),
                                        10 to stringResource(R.string.create_task_reminder_10m),
                                        30 to stringResource(R.string.create_task_reminder_30m),
                                        60 to stringResource(R.string.create_task_reminder_1h),
                                        1440 to "1 day before"
                                    ).forEach { (minutes, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                selectedReminderMinutes = minutes
                                                reminderDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Repeat / Recurrence
                    Text(
                        text = stringResource(R.string.create_task_recurrence_label),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            RecurrenceType.NONE to "None",
                            RecurrenceType.DAILY to "Daily",
                            RecurrenceType.WEEKLY to "Weekly",
                            RecurrenceType.MONTHLY to "Monthly",
                            RecurrenceType.YEARLY to "Yearly"
                        ).forEach { (type, label) ->
                            val isSelected = selectedRecurrence == type
                            Surface(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .clickable { selectedRecurrence = type },
                                shape = RoundedCornerShape(19.dp),
                                color = if (isSelected) TaskFlowPrimary else Color.Transparent,
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF374151)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Bottom Button: + Create Task ────────────────────────────────
            Button(
                onClick = { onSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskFlowPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.create_task_action_button),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
