package com.team.taskmanagementapp.data.model.stats

import com.team.taskmanagementapp.data.local.entity.Task

data class TaskFocusSummary(
    val taskTitle: String,
    val totalMinutes: Int,
    val sessionCount: Int
)

fun formatFocusDuration(minutes: Int): String {
    if (minutes <= 0) return "0m"
    val hours = minutes / 60
    val remaining = minutes % 60
    return when {
        hours == 0 -> "${remaining}m"
        remaining == 0 -> "${hours}h"
        else -> "${hours}h ${remaining}m"
    }
}

internal fun buildTopTaskSummaries(
    focusStatsByTask: List<TaskFocusStats>,
    tasks: List<Task>
): List<TaskFocusSummary> {
    val titleByTaskId = tasks.associate { it.id.toLong() to it.title }
    return focusStatsByTask
        .mapNotNull { stats ->
            val title = titleByTaskId[stats.taskId] ?: return@mapNotNull null
            TaskFocusSummary(title, stats.totalMinutes, stats.sessionCount)
        }
        .sortedWith(
            compareByDescending<TaskFocusSummary> { it.totalMinutes }
                .thenByDescending { it.sessionCount }
                .thenBy { it.taskTitle }
        )
        .take(3)
}
