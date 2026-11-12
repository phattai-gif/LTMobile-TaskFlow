package com.team.taskmanagementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.team.taskmanagementapp.ui.TaskFlowApp
import com.team.taskmanagementapp.ui.theme.TaskFLowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            TaskFLowTheme {
                TaskFlowApp()
            }
        }
    }
}
