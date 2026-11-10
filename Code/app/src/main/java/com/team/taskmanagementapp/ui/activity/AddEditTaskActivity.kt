package com.team.taskmanagementapp.ui.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.RecurrenceType
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.data.repository.TaskRepository
import com.team.taskmanagementapp.databinding.ActivityAddEditTaskBinding
import com.team.taskmanagementapp.ui.viewmodel.AddEditTaskState
import com.team.taskmanagementapp.ui.viewmodel.AddEditTaskViewModel
import com.team.taskmanagementapp.ui.viewmodel.AddEditTaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditTaskBinding

    private val viewModel: AddEditTaskViewModel by viewModels {
        val database = AppDatabase.getInstance(this)
        val repository = TaskRepository(database.taskDao())
        AddEditTaskViewModelFactory(repository)
    }

    private val taskId: Long by lazy {
        intent.getLongExtra(EXTRA_TASK_ID, -1L)
    }

    private var isEditMode = false
    private var hasLoadedTask = false
    private var selectedDueDate: Long? = null
    private var selectedDueTime: Long? = null

    private val priorities = Priority.values()
    private val statuses = TaskStatus.values()
    private val recurrenceTypes = RecurrenceType.values()
    private val reminderValues = intArrayOf(0, 5, 10, 15, 30, 60)
    private val reminderLabels = listOf(
        "Không nhắc",
        "5 phút trước",
        "10 phút trước",
        "15 phút trước",
        "30 phút trước",
        "60 phút trước"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditMode = taskId > 0L
        setupForm()
        observeViewModel()

        if (isEditMode) {
            binding.formTitle.text = "Chỉnh sửa công việc"
            binding.saveTaskButton.text = "Cập nhật"
            viewModel.loadTask(taskId)
        }
    }

    private fun setupForm() {
        setSpinnerItems(binding.prioritySpinner, priorities) { enumLabel(it.name) }
        setSpinnerItems(binding.statusSpinner, statuses) { enumLabel(it.name) }
        setSpinnerItems(binding.recurrenceSpinner, recurrenceTypes) { enumLabel(it.name) }
        setStringSpinnerItems(binding.reminderSpinner, reminderLabels)

        binding.prioritySpinner.setSelection(priorities.indexOf(Priority.MEDIUM))
        binding.statusSpinner.setSelection(statuses.indexOf(TaskStatus.TODO))
        binding.recurrenceSpinner.setSelection(recurrenceTypes.indexOf(RecurrenceType.NONE))

        binding.dueDateButton.setOnClickListener { showDatePicker() }
        binding.dueTimeButton.setOnClickListener { showTimePicker() }
        binding.saveTaskButton.setOnClickListener { saveTask() }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                AddEditTaskState.Idle -> Unit
                AddEditTaskState.Loading,
                AddEditTaskState.Saving -> {
                    binding.saveTaskButton.isEnabled = false
                }

                is AddEditTaskState.Loaded -> {
                    binding.saveTaskButton.isEnabled = true
                    if (!hasLoadedTask) {
                        hasLoadedTask = true
                        populateForm(state.task)
                    }
                }

                is AddEditTaskState.Saved -> {
                    Toast.makeText(this, "Đã lưu công việc.", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }

                is AddEditTaskState.Error -> {
                    binding.saveTaskButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()

                    if (isEditMode && !hasLoadedTask) {
                        finish()
                    }
                }
            }
        }
    }

    private fun showDatePicker() {
        val initialDate = selectedDueDate ?: System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = initialDate
        }

        val dialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val newDate = makeDate(year, month, dayOfMonth)
                val oldTime = selectedDueTime?.let {
                    Calendar.getInstance().apply { timeInMillis = it }
                }

                selectedDueDate = newDate
                selectedDueTime = oldTime?.let {
                    makeDateTime(
                        newDate,
                        it.get(Calendar.HOUR_OF_DAY),
                        it.get(Calendar.MINUTE)
                    )
                }

                updateDateButton()
                updateTimeButton()
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
        val dueDate = selectedDueDate
        if (dueDate == null) {
            Toast.makeText(this, "Hãy chọn ngày hạn trước.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentTime = selectedDueTime?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        } ?: Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedDueTime = makeDateTime(dueDate, hourOfDay, minute)
                updateTimeButton()
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveTask() {
        val title = binding.titleEditText.text?.toString().orEmpty()
        val description = binding.descriptionEditText.text?.toString().orEmpty()
        val dueDate = selectedDueDate

        binding.titleEditText.error = null
        binding.descriptionEditText.error = null

        if (title.isBlank()) {
            binding.titleEditText.error = "Vui lòng nhập tiêu đề."
            binding.titleEditText.requestFocus()
            return
        }

        if (title.length > 200) {
            binding.titleEditText.error = "Tiêu đề tối đa 200 ký tự."
            return
        }

        if (description.length > 1000) {
            binding.descriptionEditText.error = "Mô tả tối đa 1000 ký tự."
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

        val time = selectedDueTime ?: 0L
        if (
            time > 0L &&
            startOfDay(dueDate) == startOfDay(System.currentTimeMillis()) &&
            time <= System.currentTimeMillis()
        ) {
            Toast.makeText(this, "Giờ hạn phải ở tương lai.", Toast.LENGTH_SHORT).show()
            return
        }

        val priority = selectedValue(
            binding.prioritySpinner,
            priorities,
            Priority.MEDIUM
        )
        val status = selectedValue(
            binding.statusSpinner,
            statuses,
            TaskStatus.TODO
        )
        val recurrence = selectedValue(
            binding.recurrenceSpinner,
            recurrenceTypes,
            RecurrenceType.NONE
        )
        val reminderMinutes = reminderValues.getOrElse(
            binding.reminderSpinner.selectedItemPosition
        ) { 0 }

        viewModel.saveTask(
            taskId = if (isEditMode) taskId else -1L,
            title = title,
            description = description,
            dueDate = startOfDay(dueDate),
            dueTime = time,
            priority = priority,
            status = status,
            recurrenceType = recurrence,
            reminderMinutes = reminderMinutes
        )
    }

    private fun populateForm(task: Task) {
        binding.titleEditText.setText(task.title)
        binding.descriptionEditText.setText(task.description)

        selectedDueDate = task.dueDate.takeIf { it > 0L }
        selectedDueTime = task.dueTime.takeIf { it > 0L }

        updateDateButton()
        updateTimeButton()

        binding.prioritySpinner.setSelection(
            priorities.indexOf(task.priority).coerceAtLeast(0)
        )
        binding.statusSpinner.setSelection(
            statuses.indexOf(task.status).coerceAtLeast(0)
        )
        binding.recurrenceSpinner.setSelection(
            recurrenceTypes.indexOf(task.recurrenceType).coerceAtLeast(0)
        )

        val reminderIndex = reminderValues.indexOf(task.reminderMinutes)
        binding.reminderSpinner.setSelection(reminderIndex.coerceAtLeast(0))
    }

    private fun updateDateButton() {
        val date = selectedDueDate
        binding.dueDateButton.text = if (date == null) {
            "Chọn ngày hạn"
        } else {
            SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault()).format(Date(date))
        }
    }

    private fun updateTimeButton() {
        val time = selectedDueTime
        binding.dueTimeButton.text = if (time == null) {
            "Chọn giờ hạn (không bắt buộc)"
        } else {
            "Giờ hạn: " +
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
        }
    }

    private fun makeDate(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun makeDateTime(dateMillis: Long, hour: Int, minute: Int): Long {
        val date = Calendar.getInstance().apply {
            timeInMillis = dateMillis
        }

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

    private fun enumLabel(value: String): String =
        value.lowercase(Locale.getDefault())
            .replaceFirstChar { it.titlecase(Locale.getDefault()) }

    private fun <T> setSpinnerItems(
        spinner: Spinner,
        values: Array<T>,
        label: (T) -> String
    ) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            values.map(label)
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setStringSpinnerItems(spinner: Spinner, values: List<String>) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            values
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun <T> selectedValue(
        spinner: Spinner,
        values: Array<T>,
        fallback: T
    ): T = values.getOrNull(spinner.selectedItemPosition) ?: fallback

    companion object {
        // Dùng cùng key "extra_task_id" với Constants.EXTRA_TASK_ID trong source.
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}