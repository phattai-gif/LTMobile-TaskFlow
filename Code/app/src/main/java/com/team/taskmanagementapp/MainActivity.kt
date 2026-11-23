package com.team.taskmanagementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.analytics.FirebaseAnalytics
import com.team.taskmanagementapp.ui.TaskFlowApp
import com.team.taskmanagementapp.ui.theme.TaskFLowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        try {
            val analytics = FirebaseAnalytics.getInstance(this)
            val params = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainActivity")
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
            }
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
        } catch (_: Exception) {}

        setContent {
            TaskFLowTheme {
                TaskFlowApp()
            }
        }
    }
}
