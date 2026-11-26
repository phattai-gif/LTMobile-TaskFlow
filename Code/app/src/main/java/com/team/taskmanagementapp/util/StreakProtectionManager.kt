package com.team.taskmanagementapp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.receiver.AlarmReceiver
import java.util.Calendar

/**
 * Manages Streak Protection settings persistence and scheduling daily reminder alarms.
 */
object StreakProtectionManager {
    private const val TAG = "StreakProtectionMgr"
    private const val PREFS_NAME = "taskflow_streak_prefs"
    private const val KEY_PROTECTION_ENABLED = "streak_protection_enabled"
    private const val KEY_REMINDER_HOUR = "streak_reminder_hour"
    private const val KEY_REMINDER_MINUTE = "streak_reminder_minute"

    const val ACTION_STREAK_REMINDER = "com.team.taskmanagementapp.action.STREAK_REMINDER"
    private const val STREAK_ALARM_REQUEST_CODE = 99999

    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PROTECTION_ENABLED, true)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PROTECTION_ENABLED, enabled).commit()
        if (enabled) {
            scheduleStreakReminder(context)
        } else {
            cancelStreakReminder(context)
        }
    }

    fun getReminderHour(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_REMINDER_HOUR, 20)
    }

    fun getReminderMinute(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_REMINDER_MINUTE, 0)
    }

    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .commit()
        if (isEnabled(context)) {
            scheduleStreakReminder(context)
        }
    }

    fun scheduleStreakReminder(context: Context) {
        if (!isEnabled(context)) return

        val hour = getReminderHour(context)
        val minute = getReminderMinute(context)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If reminder time has already passed today, schedule for tomorrow at the same time
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val triggerAt = calendar.timeInMillis
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_STREAK_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            STREAK_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ) ?: return

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                    Log.d(TAG, "Scheduled exact streak reminder at ${calendar.time}")
                    return
                } catch (_: SecurityException) {}
            }
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            Log.d(TAG, "Scheduled streak reminder at ${calendar.time}")
        } catch (e: Exception) {
            Log.e(TAG, "Could not schedule streak reminder", e)
        }
    }

    fun cancelStreakReminder(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_STREAK_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            STREAK_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Cancelled streak reminder")
    }

    suspend fun checkAndShowStreakReminder(context: Context) {
        if (!isEnabled(context)) return

        try {
            val dao = AppDatabase.getInstance(context).taskDao()
            val allTasks = dao.getAllTasksSync()
            val now = System.currentTimeMillis()
            val streakResult = StreakCalculator.calculateForEntities(allTasks, now)

            // Rule: User has an active streak (>= 1) but hasn't completed any task today yet!
            val startOfToday = DateTimeUtils.getStartOfToday()
            val endOfToday = DateTimeUtils.getEndOfToday()
            val completedToday = allTasks.any { task ->
                val compAt = task.completedAt
                compAt != null && compAt in startOfToday..endOfToday
            }

            if (streakResult.currentStreak > 0 && !completedToday) {
                NotificationHelper.showStreakProtectionReminder(context, streakResult.currentStreak)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking streak reminder", e)
        }
    }
}
