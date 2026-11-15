package com.team.taskmanagementapp.util

import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.team.taskmanagementapp.data.local.db.AppDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal object TaskReminderRecovery {
    private const val TAG = "TaskReminderRecovery"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val scheduledTimes = mutableMapOf<Int, Long>()

    fun observeTasks(context: Context) {
        val app = context.applicationContext
        scope.launch {
            AppDatabase.getInstance(app).taskDao().getAllTasks()
                .catch { error -> Log.e(TAG, "Could not observe tasks", error) }
                .collect { sync(app, restore = false) }
        }
    }

    fun restore(context: Context, pendingResult: BroadcastReceiver.PendingResult) {
        val app = context.applicationContext
        scope.launch {
            try {
                sync(app, restore = true)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun sync(context: Context, restore: Boolean) {
        try {
            mutex.withLock {
                val database = AppDatabase.getInstance(context)
                val dao = database.taskDao()
                // Read the latest rows inside the lock so a queued observation cannot undo recovery.
                val tasks = if (restore) {
                    database.withTransaction {
                        val now = System.currentTimeMillis()
                        dao.getActiveTasksSync().map { task ->
                            val status = TaskReminderTiming.statusAfterClockChange(task, now)
                            if (status == task.status) {
                                task
                            } else {
                                task.copy(status = status, updatedAt = maxOf(now, task.updatedAt))
                                    .also { dao.updateTask(it) }
                            }
                        }
                    }
                } else {
                    dao.getActiveTasksSync()
                }

                val now = System.currentTimeMillis()
                val activeIds = tasks.mapTo(mutableSetOf()) { it.id }
                for (taskId in scheduledTimes.keys - activeIds) {
                    AlarmScheduler.cancel(context, taskId)
                    scheduledTimes.remove(taskId)
                }

                for (task in tasks) {
                    val triggerAt = TaskReminderTiming.reminderAt(task)
                    val scheduledAt = scheduledTimes[task.id]
                    if (!restore && triggerAt != null && scheduledAt == triggerAt) {
                        // A matching inexact alarm may still be waiting for delivery.
                        continue
                    }
                    if (triggerAt != null && triggerAt > now &&
                        AlarmScheduler.schedule(context, task.id, triggerAt)
                    ) {
                        scheduledTimes[task.id] = triggerAt
                    } else if (restore || scheduledAt != null || triggerAt == null) {
                        AlarmScheduler.cancel(context, task.id)
                        scheduledTimes.remove(task.id)
                    }
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e(TAG, "Could not synchronize task reminders", error)
        }
    }
}
