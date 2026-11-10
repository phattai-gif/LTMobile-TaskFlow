package com.team.taskmanagementapp.ui.home

import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RepositoryHomeTaskSource(
    private val repository: TaskRepository
) : HomeTaskSource {

    override fun observeTasks(): Flow<List<HomeTaskUiModel>> {
        return repository.getAllTasks().map { tasks ->
            tasks.map { task ->
                task.toHomeTaskUiModel()
            }
        }
    }

    override fun observeTask(
        taskId: Long
    ): Flow<HomeTaskUiModel?> {
        return repository.observeTaskById(taskId).map { task ->
            task?.toHomeTaskUiModel()
        }
    }

    override suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean) {
        val task = repository.getTaskById(taskId) ?: return
        val now = System.currentTimeMillis()
        val updatedTask = task.copy(
            isCompleted = isCompleted,
            status = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.TODO,
            completedAt = if (isCompleted) now else null,
            updatedAt = now
        )
        repository.update(updatedTask)
        repository.handleRecurringCompletion(
            originalTask = task,
            targetCompleted = isCompleted,
            now = now
        )
    }

    override suspend fun deleteTask(taskId: Long) {
        val task = repository.getTaskById(taskId) ?: return
        repository.delete(task)
    }
}

private fun Task.toHomeTaskUiModel(): HomeTaskUiModel {
    val homePriority = when (priority) {
        Priority.LOW -> HomeTaskPriority.LOW
        Priority.MEDIUM -> HomeTaskPriority.MEDIUM
        Priority.HIGH -> HomeTaskPriority.HIGH
        Priority.URGENT -> HomeTaskPriority.URGENT
    }

    val taskIsCompleted = isCompleted || status == TaskStatus.COMPLETED

    val homeStatus = if (taskIsCompleted) {
        HomeTaskStatus.COMPLETED
    } else {
        when (status) {
            TaskStatus.TODO -> HomeTaskStatus.PENDING
            TaskStatus.IN_PROGRESS -> HomeTaskStatus.IN_PROGRESS
            TaskStatus.COMPLETED -> HomeTaskStatus.COMPLETED
            TaskStatus.OVERDUE -> HomeTaskStatus.OVERDUE
        }
    }

    return HomeTaskUiModel(
        id = id.toLong(),
        title = title,
        description = description,
        dueDate = dueDate,
        dueTime = dueTime.takeIf { it > 0L },
        priority = homePriority,
        status = homeStatus,
        isCompleted = taskIsCompleted,
        completedAt = completedAt
    )
}
