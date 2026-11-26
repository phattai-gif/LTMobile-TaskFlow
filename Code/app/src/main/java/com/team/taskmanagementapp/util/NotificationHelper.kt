package com.team.taskmanagementapp.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.team.taskmanagementapp.MainActivity
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.data.local.entity.Task

internal object NotificationHelper {
    private const val CHANNEL_ID = "task_reminders"

    fun showReminder(context: Context, task: Task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (!manager.areNotificationsEnabled()) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.task_reminder_channel),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nav_calendar)
            .setContentTitle(task.title)
            .setContentText(context.getString(R.string.task_reminder_message))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        manager.notify(task.id, notification)
    }

    private const val STREAK_CHANNEL_ID = "streak_protection_channel"

    fun showStreakProtectionReminder(context: Context, currentStreak: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (!manager.areNotificationsEnabled()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                STREAK_CHANNEL_ID,
                "Streak Protection",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to maintain your active daily streak"
            }
            manager.createNotificationChannel(channel)
        }

        val openApp = PendingIntent.getActivity(
            context,
            9998,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, STREAK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nav_calendar)
            .setContentTitle("Bảo vệ chuỗi Streak của bạn! 🔥")
            .setContentText("Bạn đang có chuỗi $currentStreak ngày! Đừng quên hoàn thành công việc hôm nay nhé.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Bạn đang giữ chuỗi $currentStreak ngày liên tiếp! Hãy hoàn thành ít nhất một task hôm nay để duy trì thành tích nhé."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        manager.notify(99999, notification)
    }
}
