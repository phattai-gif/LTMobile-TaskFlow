package com.team.taskmanagementapp.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TaskRemoteViewsFactory(applicationContext)
    }
}

class TaskRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var tasks: List<Task> = emptyList()
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate() {}

    override fun onDataSetChanged() {
        // Query today's tasks synchronously on binder thread
        try {
            val dao = AppDatabase.getInstance(context).taskDao()
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = calendar.timeInMillis

            // Fetch tasks for today + any overdue uncompleted tasks
            val allTasks = kotlinx.coroutines.runBlocking {
                dao.getAllTasksSync()
            }
            tasks = allTasks.filter { task ->
                val isToday = task.dueDate in startOfDay..endOfDay
                val isOverdueUnfinished = task.dueDate < startOfDay && !task.isCompleted && task.status != TaskStatus.COMPLETED
                isToday || isOverdueUnfinished
            }.sortedWith(
                compareBy<Task> { it.isCompleted || it.status == TaskStatus.COMPLETED }
                    .thenBy { it.dueTime }
                    .thenBy { it.dueDate }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            tasks = emptyList()
        }
    }

    override fun onDestroy() {
        tasks = emptyList()
    }

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= tasks.size) return null
        val task = tasks[position]
        val isDone = task.isCompleted || task.status == TaskStatus.COMPLETED

        val views = RemoteViews(context.packageName, R.layout.item_widget_task)

        // Title with strikethrough if completed
        if (isDone) {
            val span = SpannableString(task.title)
            span.setSpan(StrikethroughSpan(), 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            views.setTextViewText(R.id.widget_item_title, span)
            views.setTextColor(R.id.widget_item_title, Color.parseColor("#94A3B8"))
            views.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_widget_check_circle)
        } else {
            views.setTextViewText(R.id.widget_item_title, task.title)
            views.setTextColor(R.id.widget_item_title, Color.parseColor("#1E293B"))
            views.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_widget_circle_outline)
        }

        // Time
        val timeStr = if (task.dueTime > 0L) {
            timeFormat.format(Date(task.dueTime))
        } else {
            "All day"
        }
        views.setTextViewText(R.id.widget_item_time, timeStr)

        // Priority badge
        val (priorityText, priorityColor) = when (task.priority) {
            Priority.URGENT -> "URGENT" to Color.parseColor("#EF4444")
            Priority.HIGH -> "HIGH" to Color.parseColor("#F97316")
            Priority.MEDIUM -> "MED" to Color.parseColor("#3B82F6")
            Priority.LOW -> "LOW" to Color.parseColor("#64748B")
        }
        views.setTextViewText(R.id.widget_item_priority, priorityText)
        views.setTextColor(R.id.widget_item_priority, priorityColor)

        // Set up FillInIntent for toggle checkbox and item row
        val fillInIntent = Intent().apply {
            putExtra(TaskWidgetProvider.EXTRA_TASK_ID, task.id)
        }
        views.setOnClickFillInIntent(R.id.widget_item_checkbox, fillInIntent)
        views.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = tasks.getOrNull(position)?.id?.toLong() ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}
