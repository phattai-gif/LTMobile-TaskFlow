package com.team.taskmanagementapp.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.team.taskmanagementapp.R

object WidgetUpdater {
    fun updateAllWidgets(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, TaskWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                for (id in appWidgetIds) {
                    TaskWidgetProvider.updateAppWidget(context, appWidgetManager, id)
                }
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
