package com.team.taskmanagementapp.data.repository

import com.team.taskmanagementapp.data.local.dao.PomodoroDao
import com.team.taskmanagementapp.data.local.entity.PomodoroSession
import com.team.taskmanagementapp.data.model.enums.SessionType
import com.team.taskmanagementapp.pomodoro.CompletedSessionRecord

internal class PomodoroRepository(private val dao: PomodoroDao) {
    suspend fun recordCompletedSession(record: CompletedSessionRecord): Boolean {
        val taskId = record.taskId ?: return false
        if (!record.isCompleted) return false

        val session = PomodoroSession(
            taskId = taskId,
            startTime = record.startTimeMillis,
            endTime = record.endTimeMillis,
            durationInMinutes = record.durationMinutes,
            sessionType = record.sessionType,
            isCompleted = true
        )
        val focusMinutes = record.durationMinutes.takeIf { record.sessionType == SessionType.FOCUS }
        return dao.recordCompletedSession(session, focusMinutes)
    }
}
