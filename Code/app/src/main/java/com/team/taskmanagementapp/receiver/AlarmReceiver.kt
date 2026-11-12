package com.team.taskmanagementapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.util.AlarmScheduler
import com.team.taskmanagementapp.util.NotificationHelper
import com.team.taskmanagementapp.util.TaskReminderTiming
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ACTION_REMINDER ||
            !intent.hasExtra(AlarmScheduler.EXTRA_TASK_ID)
        ) {
            return
        }
        val taskId = intent.getIntExtra(AlarmScheduler.EXTRA_TASK_ID, 0)
        val triggerAt = intent.getLongExtra(AlarmScheduler.EXTRA_TRIGGER_AT, 0)
        if (triggerAt <= 0L) return

        val app = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = AppDatabase.getInstance(app).taskDao().getTaskById(taskId.toLong())
                    ?: return@launch
                val now = System.currentTimeMillis()
                if (TaskReminderTiming.reminderAt(task) == triggerAt &&
                    now in triggerAt..TaskReminderTiming.deadline(task)
                ) {
                    NotificationHelper.showReminder(app, task)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("AlarmReceiver", "Could not show reminder for task $taskId", error)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
