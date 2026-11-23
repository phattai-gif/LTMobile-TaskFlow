package com.team.taskmanagementapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.analytics.FirebaseAnalytics
import com.team.taskmanagementapp.pomodoro.PomodoroService
import com.team.taskmanagementapp.ui.TaskFlowApp
import com.team.taskmanagementapp.ui.theme.TaskFLowTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val openPomodoro = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        handlePomodoroIntent(intent)

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
                TaskFlowApp(
                    openPomodoro = openPomodoro,
                    onPomodoroOpened = { openPomodoro.value = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePomodoroIntent(intent)
    }

    private fun handlePomodoroIntent(intent: Intent?) {
        if (intent?.action == PomodoroService.ACTION_OPEN_TIMER) {
            intent.action = null
            openPomodoro.value = true
        }
    }
}
