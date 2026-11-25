package com.team.taskmanagementapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import com.team.taskmanagementapp.pomodoro.PomodoroService
import com.team.taskmanagementapp.ui.TaskFlowApp
import com.team.taskmanagementapp.ui.theme.TaskFLowTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : FragmentActivity() {
    companion object {
        const val ACTION_OPEN_TASK_DETAIL = "com.team.taskmanagementapp.ACTION_OPEN_TASK_DETAIL"
        const val EXTRA_VIEW_TASK_ID = "extra_view_task_id"
    }

    private val openPomodoro = MutableStateFlow(false)
    private val openTaskId = MutableStateFlow<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        handleIntent(intent)

        try {
            Firebase.initialize(context = this)
            Firebase.appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance(),
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

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
                    onPomodoroOpened = { openPomodoro.value = false },
                    openTaskId = openTaskId,
                    onTaskOpened = { openTaskId.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == PomodoroService.ACTION_OPEN_TIMER) {
            intent.action = null
            openPomodoro.value = true
        }
        val taskId = intent?.getLongExtra(EXTRA_VIEW_TASK_ID, -1L) ?: -1L
        if (taskId > 0L) {
            intent?.removeExtra(EXTRA_VIEW_TASK_ID)
            openTaskId.value = taskId
        }
    }
}
