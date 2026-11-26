package com.team.taskmanagementapp

import android.app.Application
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.team.taskmanagementapp.util.TaskReminderRecovery

class TaskApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        TaskReminderRecovery.observeTasks(this)
        com.team.taskmanagementapp.util.StreakProtectionManager.scheduleStreakReminder(this)

        // Observe database changes to automatically refresh Home Screen Widgets
        try {
            val db = com.team.taskmanagementapp.data.local.db.AppDatabase.getInstance(this)
            db.invalidationTracker.addObserver(
                object : androidx.room.InvalidationTracker.Observer("tasks") {
                    override fun onInvalidated(tables: Set<String>) {
                        com.team.taskmanagementapp.widget.WidgetUpdater.updateAllWidgets(this@TaskApplication)
                    }
                }
            )
        } catch (_: Exception) {}

        // Initialize Firebase Analytics & log immediate App Open event
        try {
            val analytics = FirebaseAnalytics.getInstance(this)
            analytics.setAnalyticsCollectionEnabled(true)
            analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, Bundle())

            // Set user tracking if an authenticated session already exists
            FirebaseAuth.getInstance().currentUser?.let { user ->
                analytics.setUserId(user.uid)
                analytics.setUserProperty("user_email", user.email ?: "")
                analytics.setUserProperty("auth_provider", user.providerData.firstOrNull()?.providerId ?: "email")
            }
        } catch (_: Exception) {}
    }
}
