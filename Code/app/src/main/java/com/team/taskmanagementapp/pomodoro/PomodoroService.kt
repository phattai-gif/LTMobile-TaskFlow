package com.team.taskmanagementapp.pomodoro

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.team.taskmanagementapp.MainActivity
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.data.repository.PomodoroRepository
import com.team.taskmanagementapp.data.repository.PomodoroSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PomodoroService : Service() {
    private val engine = PomodoroTimerController.engine
    private val settings by lazy { PomodoroSettingsRepository.from(this) }
    private val sessions by lazy { PomodoroRepository(AppDatabase.getInstance(this).pomodoroDao()) }
    private val notifications by lazy { getSystemService(NotificationManager::class.java) }
    private val alarms by lazy { getSystemService(AlarmManager::class.java) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val completions = PomodoroCompletionTracker()
    private var ticker: Job? = null
    private var scheduledEnd = 0L
    private var foreground = false
    private val endAlarm by lazy { actionIntent(ACTION_TICK) }
    private val openTimer by lazy {
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .setAction(ACTION_OPEN_TIMER)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onCreate() {
        super.onCreate()
        notifications.createNotificationChannels(
            listOf(
                NotificationChannel(
                    TIMER_CHANNEL, getString(R.string.pomodoro_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ),
                NotificationChannel(
                    COMPLETION_CHANNEL, getString(R.string.pomodoro_alert_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { enableVibration(true) }
            )
        )
        completions.syncTo(engine.currentSnapshot.completionId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_START) {
            engine.updateConfig(settings.load())
        }

        when (action) {
            ACTION_START -> engine.start(intent.getLongExtra(EXTRA_TASK_ID, -1L).takeIf { it > 0L })
            ACTION_PAUSE -> engine.pause()
            ACTION_RESUME -> engine.resume()
            ACTION_SKIP -> engine.skip()
            ACTION_STOP -> {
                engine.stop()
                completions.syncTo(engine.currentSnapshot.completionId)
            }
        }

        val snapshot = engine.currentSnapshot
        if (snapshot.state == PomodoroTimerState.IDLE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            foreground = false
            syncEndAlarm(0L)
            notifications.cancel(COMPLETION_NOTIFICATION_ID)
            stopSelf(startId)
            return START_NOT_STICKY
        }

        if (!foreground || action == ACTION_START) {
            startForeground(TIMER_NOTIFICATION_ID, timerNotification(snapshot))
            foreground = true
        }
        refreshTimer()
        if (engine.currentSnapshot.state == PomodoroTimerState.RUNNING) {
            if (ticker?.isActive != true) {
                ticker = scope.launch {
                    while (isActive && engine.currentSnapshot.state == PomodoroTimerState.RUNNING) {
                        delay(1_000L)
                        refreshTimer()
                    }
                }
            }
        } else {
            ticker?.cancel()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        alarms.cancel(endAlarm)
        if (engine.currentSnapshot.state == PomodoroTimerState.RUNNING) engine.pause()
        super.onDestroy()
    }

    private fun refreshTimer() {
        engine.tick()
        val snapshot = engine.currentSnapshot
        completions.consumeCompletedSession(snapshot)?.let { record ->
            // The database transaction can finish even if Stop closes this service immediately.
            recordingScope.launch {
                try {
                    sessions.recordCompletedSession(record)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Log.e(TAG, "Unable to save completed Pomodoro session", error)
                }
            }
            if (canNotify()) {
                val nextType = if (snapshot.state == PomodoroTimerState.RUNNING) {
                    snapshot.sessionType
                } else {
                    snapshot.nextSessionType
                }
                notifications.notify(
                    COMPLETION_NOTIFICATION_ID,
                    NotificationCompat.Builder(this, COMPLETION_CHANNEL)
                        .setSmallIcon(R.drawable.ic_notification_pomodoro)
                        .setContentTitle(getString(R.string.pomodoro_completion_title, getString(record.sessionType.pomodoroLabel)))
                        .setContentText(getString(
                            R.string.pomodoro_completion_body, getString(nextType.pomodoroLabel),
                            snapshot.config.durationMinutesFor(nextType)
                        ))
                        .setContentIntent(openTimer)
                        .setAutoCancel(true)
                        .build()
                )
            }
        }
        syncEndAlarm(snapshot.targetEndElapsedRealtime)
        if (canNotify()) notifications.notify(TIMER_NOTIFICATION_ID, timerNotification(snapshot))
    }

    private fun syncEndAlarm(target: Long) {
        if (target == scheduledEnd) return

        alarms.cancel(endAlarm)
        scheduledEnd = target
        if (target <= 0L) return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()) {
            try {
                alarms.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, target, endAlarm)
                return
            } catch (_: SecurityException) {
                // The exact-alarm permission may change while a session is active.
            }
        }
        alarms.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, target, endAlarm)
    }

    private fun timerNotification(snapshot: PomodoroSnapshot): Notification {
        val title = getString(
            R.string.pomodoro_notification_title,
            getString(snapshot.sessionType.pomodoroLabel),
            snapshot.currentCycle.coerceAtLeast(1),
            snapshot.totalCycles
        )
        val description = if (snapshot.state == PomodoroTimerState.COMPLETED) {
            getString(R.string.pomodoro_notification_next_session, getString(snapshot.nextSessionType.pomodoroLabel))
        } else {
            getString(R.string.pomodoro_notification_time_status, snapshot.formattedRemaining,
                getString(snapshot.state.pomodoroLabel))
        }

        return NotificationCompat.Builder(this, TIMER_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification_pomodoro)
            .setContentTitle(title)
            .setContentText(description)
            .setContentIntent(openTimer)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .apply {
                when (snapshot.state) {
                    PomodoroTimerState.RUNNING -> addTimerAction(ACTION_PAUSE, R.string.pomodoro_action_pause)
                    PomodoroTimerState.PAUSED -> addTimerAction(ACTION_RESUME, R.string.pomodoro_action_resume)
                    PomodoroTimerState.COMPLETED -> addTimerAction(ACTION_RESUME, R.string.pomodoro_action_start_next)
                    PomodoroTimerState.IDLE -> Unit
                }
                if (snapshot.isActive) addTimerAction(ACTION_SKIP, R.string.pomodoro_action_skip)
                addTimerAction(ACTION_STOP, R.string.pomodoro_action_stop)
            }
            .build()
    }

    private fun NotificationCompat.Builder.addTimerAction(action: String, label: Int) {
        addAction(R.drawable.ic_notification_pomodoro, getString(label), actionIntent(action))
    }

    private fun actionIntent(action: String): PendingIntent =
        PendingIntent.getService(this, 0, serviceIntent(this, action), PendingIntent.FLAG_IMMUTABLE)

    private fun canNotify(): Boolean =
        (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) && notifications.areNotificationsEnabled()

    companion object {
        internal const val ACTION_OPEN_TIMER = "com.team.taskmanagementapp.action.OPEN_POMODORO"
        private const val TAG = "PomodoroService"
        private const val TIMER_CHANNEL = "pomodoro_timer_channel"
        private const val COMPLETION_CHANNEL = "pomodoro_alert_channel"
        private const val TIMER_NOTIFICATION_ID = -1001
        private const val COMPLETION_NOTIFICATION_ID = -1002
        private const val EXTRA_TASK_ID = "extra_pomodoro_task_id"
        private const val ACTION_START = "pomodoro.START"
        private const val ACTION_PAUSE = "pomodoro.PAUSE"
        private const val ACTION_RESUME = "pomodoro.RESUME"
        private const val ACTION_SKIP = "pomodoro.SKIP"
        private const val ACTION_STOP = "pomodoro.STOP"
        private const val ACTION_TICK = "pomodoro.TICK"
        private val recordingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun start(context: Context, taskId: Long) {
            ContextCompat.startForegroundService(
                context, serviceIntent(context, ACTION_START).putExtra(EXTRA_TASK_ID, taskId)
            )
        }

        fun pause(context: Context) = sendAction(context, ACTION_PAUSE)
        fun resume(context: Context) = sendAction(context, ACTION_RESUME)
        fun skip(context: Context) = sendAction(context, ACTION_SKIP)
        fun stop(context: Context) = sendAction(context, ACTION_STOP)

        private fun sendAction(context: Context, action: String) {
            context.startService(serviceIntent(context, action))
        }

        private fun serviceIntent(context: Context, action: String): Intent =
            Intent(context, PomodoroService::class.java).setAction(action)
    }
}
