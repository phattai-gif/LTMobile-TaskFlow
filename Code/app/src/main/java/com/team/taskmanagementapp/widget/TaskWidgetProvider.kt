package com.team.taskmanagementapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.team.taskmanagementapp.MainActivity
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.ui.activity.AddEditTaskActivity
import com.team.taskmanagementapp.util.TaskCompletionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_TASK = "com.team.taskmanagementapp.widget.ACTION_TOGGLE_TASK"
        const val ACTION_REFRESH_WIDGET = "com.team.taskmanagementapp.widget.ACTION_REFRESH_WIDGET"
        const val EXTRA_TASK_ID = "com.team.taskmanagementapp.widget.EXTRA_TASK_ID"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_task_list)

            // Current date formatted e.g. "Tuesday, 24 Nov"
            val dateFormat = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())
            views.setTextViewText(R.id.widget_date, dateFormat.format(Date()))

            // Set up ListView service intent with unique Uri to avoid intent caching
            val serviceIntent = Intent(context, TaskWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse("taskflow://widget/id/$appWidgetId")
            }
            views.setRemoteAdapter(R.id.widget_list_view, serviceIntent)
            views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_view)

            // PendingIntent for clicking App Title or Header (opens MainActivity)
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                100,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_date, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_empty_view, openAppPendingIntent)

            // PendingIntent for clicking "+" (opens AddEditTaskActivity)
            val addIntent = Intent(context, AddEditTaskActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val addPendingIntent = PendingIntent.getActivity(
                context,
                0,
                addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_add, addPendingIntent)

            // PendingIntent for clicking Refresh
            val refreshIntent = Intent(context, TaskWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

            // PendingIntent template for items in the list
            val itemClickIntent = Intent(context, TaskWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_TASK
            }
            val itemClickPendingIntent = PendingIntent.getBroadcast(
                context,
                2,
                itemClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list_view, itemClickPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TOGGLE_TASK -> {
                val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
                if (taskId != -1) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            TaskCompletionHelper.toggleById(context, taskId)
                            WidgetUpdater.updateAllWidgets(context)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
            ACTION_REFRESH_WIDGET -> {
                WidgetUpdater.updateAllWidgets(context)
            }
        }
    }
}
