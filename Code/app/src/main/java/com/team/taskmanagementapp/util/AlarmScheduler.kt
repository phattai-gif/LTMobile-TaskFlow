package com.team.taskmanagementapp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.team.taskmanagementapp.receiver.AlarmReceiver

internal object AlarmScheduler {
    const val ACTION_REMINDER = "com.team.taskmanagementapp.action.TASK_REMINDER"
    const val EXTRA_TASK_ID = "task_id"
    const val EXTRA_TRIGGER_AT = "trigger_at"
    private const val TAG = "AlarmScheduler"

    fun schedule(context: Context, taskId: Int, triggerAt: Long): Boolean {
        if (triggerAt <= System.currentTimeMillis()) return false

        val manager = context.getSystemService(AlarmManager::class.java) ?: return false
        val alarm = PendingIntent.getBroadcast(
            context,
            taskId,
            reminderIntent(context).apply {
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TRIGGER_AT, triggerAt)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ) ?: return false

        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()) {
                try {
                    manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, alarm)
                    return true
                } catch (_: SecurityException) {
                    // The permission may have been revoked after it was checked.
                }
            }
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, alarm)
            true
        } catch (error: RuntimeException) {
            Log.w(TAG, "Could not schedule reminder for task $taskId", error)
            false
        }
    }

    fun cancel(context: Context, taskId: Int) {
        val alarm = PendingIntent.getBroadcast(
            context,
            taskId,
            reminderIntent(context),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        context.getSystemService(AlarmManager::class.java)?.cancel(alarm)
        alarm.cancel()
    }

    private fun reminderIntent(context: Context) =
        Intent(context, AlarmReceiver::class.java).setAction(ACTION_REMINDER)
}
