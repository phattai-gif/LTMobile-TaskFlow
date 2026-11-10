package com.team.taskmanagementapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.team.taskmanagementapp.ui.TaskFlowApp
import com.team.taskmanagementapp.ui.activity.AddEditTaskActivity
import com.team.taskmanagementapp.ui.theme.TaskFLowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskFLowTheme {
                TaskFlowApp(
                    onCreateTask = { openTaskEditor() },
                    onEditTask = { taskId -> openTaskEditor(taskId) }
                )
            }
        }
    }

    private fun openTaskEditor(taskId: Long? = null) {
        val editorIntent = Intent(this, AddEditTaskActivity::class.java)
        if (taskId != null) {
            editorIntent.putExtra(AddEditTaskActivity.EXTRA_TASK_ID, taskId)
        }
        startActivity(editorIntent)
    }
}
