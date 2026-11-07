package com.team.taskmanagementapp.data.model.stats

data class TaskFocusStats(
    val taskId: Long,
    val totalMinutes: Int,
    val sessionCount: Int
)