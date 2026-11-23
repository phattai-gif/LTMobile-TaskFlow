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
