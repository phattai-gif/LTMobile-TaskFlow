package com.team.taskmanagementapp

import android.app.Application
import com.team.taskmanagementapp.util.TaskReminderRecovery

class TaskApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskReminderRecovery.observeTasks(this)
    }
}
