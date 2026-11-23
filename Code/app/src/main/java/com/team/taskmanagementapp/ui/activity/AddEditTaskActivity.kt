package com.team.taskmanagementapp.ui.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.RecurrenceType
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.data.repository.TaskRepository
import com.team.taskmanagementapp.ui.theme.TaskFLowTheme
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary
import com.team.taskmanagementapp.ui.viewmodel.AddEditTaskState
import com.team.taskmanagementapp.ui.viewmodel.AddEditTaskViewModel
import com.team.taskmanagementapp.ui.viewmodel.AddEditTaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditTaskActivity : AppCompatActivity() {

    private val viewModel: AddEditTaskViewModel by viewModels {
        val database = AppDatabase.getInstance(this)
        val repository = TaskRepository(database.taskDao())
        AddEditTaskViewModelFactory(repository)
    }

    private var taskId: Long = -1L
    private var isEditMode: Boolean = false

    // Form states
    private var formTitle by mutableStateOf("")
    private var formDescription by mutableStateOf("")
    private var formDueDate by mutableStateOf<Long?>(null)
    private var formDueTime by mutableStateOf<Long?>(null)
    private var formPriority by mutableStateOf(Priority.MEDIUM)
    private var formStatus by mutableStateOf(TaskStatus.TODO)
    private var formRecurrence by mutableStateOf(RecurrenceType.NONE)
    private var formReminderMinutes by mutableIntStateOf(30)
    private var isSaving by mutableStateOf(false)
    private var hasLoadedTask = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        isEditMode = taskId > 0L

        observeViewModel()

        if (isEditMode) {
            viewModel.loadTask(taskId)
        } else {
            formDueDate = startOfDay(System.currentTimeMillis())
            formDueTime = System.currentTimeMillis() + 3600000L
        }

        setContent {
            TaskFLowTheme {
                EditTaskContent(
                    isEditMode = isEditMode,
                    title = formTitle,
                    onTitleChange = { formTitle = it },
                    description = formDescription,
                    onDescriptionChange = { formDescription = it },
                    dueDate = formDueDate,
                    dueTime = formDueTime,
                    priority = formPriority,
                    onPriorityChange = { formPriority = it },
                    status = formStatus,
                    onStatusChange = { formStatus = it },
                    recurrence = formRecurrence,
                    onRecurrenceChange = { formRecurrence = it },
                    reminderMinutes = formReminderMinutes,
                    onReminderChange = { formReminderMinutes = it },
                    isSaving = isSaving,
                    onPickDate = { showDatePicker() },
                    onPickTime = { showTimePicker() },
                    onSave = { saveTask() },
                    onBack = { finish() }
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                AddEditTaskState.Idle -> {
                    isSaving = false
                }
                AddEditTaskState.Loading,
                AddEditTaskState.Saving -> {
                    isSaving = true
                }
                is AddEditTaskState.Loaded -> {
                    isSaving = false
                    if (!hasLoadedTask) {
                        hasLoadedTask = true
                        populateForm(state.task)
                    }
                }
                is AddEditTaskState.Saved -> {
                    isSaving = false
                    Toast.makeText(this, "Đã lưu công việc thành công.", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is AddEditTaskState.Error -> {
                    isSaving = false
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    if (isEditMode && !hasLoadedTask) {
                        finish()
                    }
                }
            }
        }
    }

    private fun populateForm(task: Task) {
        formTitle = task.title
        formDescription = task.description
        formDueDate = task.dueDate.takeIf { it > 0L }
        formDueTime = task.dueTime.takeIf { it > 0L }
        formPriority = task.priority
        formStatus = task.status
        formRecurrence = task.recurrenceType
        formReminderMinutes = task.reminderMinutes
    }

    private fun showDatePicker() {
        val initialDate = formDueDate ?: System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = initialDate }

        val dialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val newDate = makeDate(year, month, dayOfMonth)
                val oldTime = formDueTime?.let {
                    Calendar.getInstance().apply { timeInMillis = it }
                }
                formDueDate = newDate
                formDueTime = oldTime?.let {
                    makeDateTime(newDate, it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE))
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        if (!isEditMode) {
            dialog.datePicker.minDate = startOfDay(System.currentTimeMillis())
        }
        dialog.show()
    }

    private fun showTimePicker() {
        val dueDate = formDueDate
        if (dueDate == null) {
            Toast.makeText(this, "Hãy chọn ngày hạn trước.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentTime = formDueTime?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        } ?: Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                formDueTime = makeDateTime(dueDate, hourOfDay, minute)
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveTask() {
        val title = formTitle.trim()
        val description = formDescription.trim()
        val dueDate = formDueDate

        if (title.isBlank()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề.", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.length > 200) {
            Toast.makeText(this, "Tiêu đề tối đa 200 ký tự.", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.length > 1000) {
            Toast.makeText(this, "Mô tả tối đa 1000 ký tự.", Toast.LENGTH_SHORT).show()
            return
        }

        if (dueDate == null) {
            Toast.makeText(this, "Vui lòng chọn ngày hạn.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isEditMode && dueDate < startOfDay(System.currentTimeMillis())) {
            Toast.makeText(this, "Ngày hạn không được ở quá khứ.", Toast.LENGTH_SHORT).show()
            return
        }

        val time = formDueTime ?: 0L
        if (
            time > 0L &&
            startOfDay(dueDate) == startOfDay(System.currentTimeMillis()) &&
            time <= System.currentTimeMillis()
        ) {
            Toast.makeText(this, "Giờ hạn phải ở tương lai.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveTask(
            taskId = if (isEditMode) taskId else -1L,
            title = title,
            description = description,
            dueDate = startOfDay(dueDate),
            dueTime = time,
            priority = formPriority,
            status = formStatus,
            recurrenceType = formRecurrence,
            reminderMinutes = formReminderMinutes
        )
    }

    private fun makeDate(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun makeDateTime(dateMillis: Long, hour: Int, minute: Int): Long {
        val date = Calendar.getInstance().apply { timeInMillis = dateMillis }
        return Calendar.getInstance().apply {
            clear()
            set(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH),
                hour,
                minute,
                0
            )
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun startOfDay(timeMillis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTaskContent(
    isEditMode: Boolean,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    dueDate: Long?,
    dueTime: Long?,
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
    status: TaskStatus,
    onStatusChange: (TaskStatus) -> Unit,
    recurrence: RecurrenceType,
    onRecurrenceChange: (RecurrenceType) -> Unit,
    reminderMinutes: Int,
    onReminderChange: (Int) -> Unit,
    isSaving: Boolean,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var reminderDropdownExpanded by remember { mutableStateOf(false) }

    fun formatDate(millis: Long?): String {
        if (millis == null || millis <= 0L) return "Chọn ngày hạn"
        val sdf = SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatTime(millis: Long?): String {
        if (millis == null || millis <= 0L) return "Chọn giờ hạn (không bắt buộc)"
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "Giờ hạn: " + sdf.format(Date(millis))
    }

    fun reminderLabel(minutes: Int): String {
        return when (minutes) {
            0 -> "Không nhắc"
            5 -> "5 phút trước"
            10 -> "10 phút trước"
            15 -> "15 phút trước"
            30 -> "30 phút trước"
            60 -> "60 phút trước"
            else -> "$minutes phút trước"
        }
    }

    fun statusLabel(st: TaskStatus): String {
        return when (st) {
            TaskStatus.TODO -> "Cần làm (Todo)"
            TaskStatus.IN_PROGRESS -> "Đang làm (In Progress)"
            TaskStatus.COMPLETED -> "Hoàn thành (Completed)"
            TaskStatus.OVERDUE -> "Quá hạn (Overdue)"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ── Top Navigation Bar ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color(0xFF1E293B)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TaskFlow",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TaskFlowPrimary
                )
            }

            // ── Screen Title & Subtitle ─────────────────────────────────────
            Text(
                text = if (isEditMode) "Chỉnh sửa công việc" else "Tạo công việc",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cập nhật chi tiết, thời hạn và độ ưu tiên cho công việc.",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Card 1: Title & Description ─────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "TIÊU ĐỀ CÔNG VIỆC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = title,
                        onValueChange = onTitleChange,
                        placeholder = {
                            Text("Ví dụ: Chuẩn bị slide thuyết trình...", color = Color(0xFF9CA3AF), fontSize = 15.sp)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = TaskFlowPrimary,
                            unfocusedIndicatorColor = Color(0xFFF1F5F9)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "MÔ TẢ CHI TIẾT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        placeholder = {
                            Text("Ghi chú thêm về công việc này (không bắt buộc)...", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                        },
                        minLines = 3,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Card 2: Due Date & Due Time ─────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "THỜI HẠN & GIỜ ĐẾN HẠN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Date picker button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .clickable { onPickDate() }
                                .padding(vertical = 12.dp, horizontal = 14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = TaskFlowPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Ngày hạn",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = formatDate(dueDate),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }

                        // Time picker button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .clickable { onPickTime() }
                                .padding(vertical = 12.dp, horizontal = 14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = TaskFlowPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Giờ hạn",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = formatTime(dueTime),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Card 3: Priority Selection ──────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "MỨC ƯU TIÊN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Priority.LOW to "Low",
                            Priority.MEDIUM to "Medium",
                            Priority.HIGH to "High",
                            Priority.URGENT to "Urgent"
                        ).forEach { (p, label) ->
                            val isSelected = priority == p
                            val selectedColor = when (p) {
                                Priority.URGENT -> Color(0xFFDC2626)
                                Priority.HIGH -> Color(0xFFEF4444)
                                Priority.MEDIUM -> Color(0xFFF59E0B)
                                Priority.LOW -> Color(0xFF10B981)
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable { onPriorityChange(p) },
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
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF374151)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Card 4: Status, Reminder & Recurrence ────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Status row
                    Text(
                        text = "TRẠNG THÁI CÔNG VIỆC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = statusDropdownExpanded,
                        onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = statusLabel(status),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            listOf(
                                TaskStatus.TODO,
                                TaskStatus.IN_PROGRESS,
                                TaskStatus.COMPLETED
                            ).forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(statusLabel(st)) },
                                    onClick = {
                                        onStatusChange(st)
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Reminder row
                    Text(
                        text = "NHẮC TRƯỚC HẠN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = reminderDropdownExpanded,
                        onExpandedChange = { reminderDropdownExpanded = !reminderDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = reminderLabel(reminderMinutes),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = reminderDropdownExpanded,
                            onDismissRequest = { reminderDropdownExpanded = false }
                        ) {
                            listOf(0, 5, 10, 15, 30, 60).forEach { mins ->
                                DropdownMenuItem(
                                    text = { Text(reminderLabel(mins)) },
                                    onClick = {
                                        onReminderChange(mins)
                                        reminderDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Recurrence row
                    Text(
                        text = "LẶP LẠI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            RecurrenceType.NONE to "Không lặp",
                            RecurrenceType.DAILY to "Hàng ngày",
                            RecurrenceType.WEEKLY to "Hàng tuần",
                            RecurrenceType.MONTHLY to "Hàng tháng"
                        ).forEach { (rec, label) ->
                            val isSelected = recurrence == rec
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .clickable { onRecurrenceChange(rec) },
                                shape = RoundedCornerShape(19.dp),
                                color = if (isSelected) TaskFlowPrimary else Color(0xFFF1F5F9)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Bottom Action Button ────────────────────────────────────────
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSave()
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TaskFlowPrimary)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSaving) "Đang lưu..." else if (isEditMode) "Cập nhật công việc" else "Tạo công việc",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
