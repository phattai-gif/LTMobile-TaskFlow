package com.team.taskmanagementapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.team.taskmanagementapp.data.model.enums.SessionType

@Entity(
    tableName = "pomodoro_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["taskId"]),
        Index(value = ["startTime"])
    ]
)
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "taskId")
    val taskId: Long,
    @ColumnInfo(name = "startTime")
    val startTime: Long,
    @ColumnInfo(name = "endTime")
    val endTime: Long,
    @ColumnInfo(name = "durationInMinutes")
    val durationInMinutes: Int,
    @ColumnInfo(name = "sessionType")
    val sessionType: SessionType = SessionType.FOCUS,
    @ColumnInfo(name = "isCompleted")
    val isCompleted: Boolean = false
)