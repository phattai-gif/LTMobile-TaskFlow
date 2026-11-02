package com.team.taskmanagementapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.team.taskmanagementapp.data.local.entity.PomodoroSession
import com.team.taskmanagementapp.data.model.stats.TaskFocusStats
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSession): Long

    @Transaction
    suspend fun recordCompletedSession(
        session: PomodoroSession,
        focusMinutes: Int?
    ): Boolean {
        if (findTaskId(session.taskId) == null) return false

        insertSession(session)

        if (focusMinutes != null) {
            addCompletedFocusSessionStats(session.taskId, focusMinutes)
        }
        return true
    }

    @Query("SELECT id FROM tasks WHERE id = :taskId")
    suspend fun findTaskId(taskId: Long): Int?

    @Query(
        """
        UPDATE tasks
        SET completedPomodoros = completedPomodoros + 1,
            totalFocusTimeMinutes = totalFocusTimeMinutes + :focusMinutes
        WHERE id = :taskId
        """
    )
    suspend fun addCompletedFocusSessionStats(
        taskId: Long,
        focusMinutes: Int
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<PomodoroSession>): List<Long>

    @Delete
    suspend fun deleteSession(session: PomodoroSession)

    @Query("DELETE FROM pomodoro_sessions WHERE taskId = :taskId")
    suspend fun deleteSessionsByTaskId(taskId: Long)

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun deleteAllSessions()

    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId ORDER BY startTime DESC")
    fun observeSessionsByTaskId(taskId: Long): Flow<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId ORDER BY startTime DESC")
    suspend fun getSessionsByTaskId(taskId: Long): List<PomodoroSession>

    @Query(
        "SELECT * FROM pomodoro_sessions " +
                "WHERE taskId = :taskId AND sessionType = 'FOCUS' ORDER BY startTime DESC"
    )
    fun observeFocusSessionsByTaskId(taskId: Long): Flow<List<PomodoroSession>>

    @Query(
        "SELECT * FROM pomodoro_sessions " +
                "WHERE taskId = :taskId AND sessionType = 'FOCUS' ORDER BY startTime DESC"
    )
    suspend fun getFocusSessionsByTaskId(taskId: Long): List<PomodoroSession>

    @Query(
        "SELECT COUNT(*) FROM pomodoro_sessions " +
                "WHERE taskId = :taskId AND sessionType = 'FOCUS' AND isCompleted = 1"
    )
    fun observeCompletedFocusSessionCountByTaskId(taskId: Long): Flow<Int>

    @Query(
        "SELECT COUNT(*) FROM pomodoro_sessions " +
                "WHERE taskId = :taskId AND sessionType = 'FOCUS' AND isCompleted = 1"
    )
    suspend fun getCompletedFocusSessionCountByTaskId(taskId: Long): Int

    @Query(
        "SELECT IFNULL(SUM(durationInMinutes), 0) FROM pomodoro_sessions " +
                "WHERE taskId = :taskId AND sessionType = 'FOCUS' AND isCompleted = 1"
    )
    suspend fun getTotalFocusMinutesByTaskId(taskId: Long): Int

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE sessionType = 'FOCUS' AND isCompleted = 1")
    fun observeTotalCompletedFocusSessions(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE sessionType = 'FOCUS' AND isCompleted = 1")
    suspend fun getTotalCompletedFocusSessions(): Int

    @Query(
        "SELECT IFNULL(SUM(durationInMinutes), 0) FROM pomodoro_sessions " +
                "WHERE sessionType = 'FOCUS' AND isCompleted = 1 " +
                "AND startTime BETWEEN :startTime AND :endTime"
    )
    fun observeTotalFocusMinutesInRange(startTime: Long, endTime: Long): Flow<Int>

    @Query(
        "SELECT IFNULL(SUM(durationInMinutes), 0) FROM pomodoro_sessions " +
                "WHERE sessionType = 'FOCUS' AND isCompleted = 1 " +
                "AND startTime BETWEEN :startTime AND :endTime"
    )
    suspend fun getTotalFocusMinutesInRange(startTime: Long, endTime: Long): Int

    @Query(
        "SELECT COUNT(*) FROM pomodoro_sessions " +
                "WHERE sessionType = 'FOCUS' AND isCompleted = 1 " +
                "AND startTime BETWEEN :startTime AND :endTime"
    )
    fun observeFocusSessionCountInRange(startTime: Long, endTime: Long): Flow<Int>

    @Query(
        "SELECT COUNT(*) FROM pomodoro_sessions " +
                "WHERE sessionType = 'FOCUS' AND isCompleted = 1 " +
                "AND startTime BETWEEN :startTime AND :endTime"
    )
    suspend fun getFocusSessionCountInRange(startTime: Long, endTime: Long): Int

    @Query(
        "SELECT * FROM pomodoro_sessions " +
                "WHERE sessionType = 'FOCUS' AND isCompleted = 1 " +
                "AND startTime BETWEEN :startTime AND :endTime ORDER BY startTime ASC"
    )
    fun observeFocusSessionsInRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<PomodoroSession>>

    @Query(
        "SELECT * FROM pomodoro_sessions " +
                "WHERE sessionType = 'FOCUS' AND isCompleted = 1 " +
                "AND startTime BETWEEN :startTime AND :endTime ORDER BY startTime ASC"
    )
    suspend fun getFocusSessionsInRange(
        startTime: Long,
        endTime: Long
    ): List<PomodoroSession>

    @Query(
        """
        SELECT taskId AS taskId,
               IFNULL(SUM(durationInMinutes), 0) AS totalMinutes,
               COUNT(*) AS sessionCount
        FROM pomodoro_sessions
        WHERE sessionType = 'FOCUS' AND isCompleted = 1
          AND startTime BETWEEN :startTime AND :endTime
        GROUP BY taskId
        ORDER BY totalMinutes DESC
        """
    )
    suspend fun getFocusStatsByTask(
        startTime: Long,
        endTime: Long
    ): List<TaskFocusStats>

    @Query(
        """
        SELECT taskId AS taskId,
               IFNULL(SUM(durationInMinutes), 0) AS totalMinutes,
               COUNT(*) AS sessionCount
        FROM pomodoro_sessions
        WHERE sessionType = 'FOCUS' AND isCompleted = 1
          AND startTime BETWEEN :startTime AND :endTime
        GROUP BY taskId
        ORDER BY totalMinutes DESC
        """
    )
    fun observeFocusStatsByTask(
        startTime: Long,
        endTime: Long
    ): Flow<List<TaskFocusStats>>

    @Query("SELECT * FROM pomodoro_sessions WHERE isCompleted = 0 ORDER BY startTime DESC")
    suspend fun getIncompleteSessions(): List<PomodoroSession>

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestSession(): PomodoroSession?

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime ASC")
    suspend fun getAllSessions(): List<PomodoroSession>
}