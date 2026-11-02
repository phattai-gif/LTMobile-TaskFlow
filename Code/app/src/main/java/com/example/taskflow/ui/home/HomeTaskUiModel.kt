package com.example.taskflow.ui.home

enum class HomeTaskPriority {
    LOW,
    MEDIUM,
    HIGH
}

enum class HomeTaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    OVERDUE
}

data class HomeTaskUiModel(
    val id: Long,
    val title: String,
    val description: String,
    val dueDate: Long,
    val dueTime: Long? = null,
    val priority: HomeTaskPriority = HomeTaskPriority.MEDIUM,
    val status: HomeTaskStatus = HomeTaskStatus.PENDING
)