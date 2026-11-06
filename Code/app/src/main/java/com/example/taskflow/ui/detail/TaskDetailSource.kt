package com.example.taskflow.ui.detail

import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

/** Dùng chung dữ liệu Room với Home; cho phép thay bằng nguồn giả khi kiểm thử. */
interface TaskDetailSource {
    fun observeTask(taskId: Long): Flow<Task?>
    suspend fun getTask(taskId: Long): Task?
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
}

class RepositoryTaskDetailSource(
    private val repository: TaskRepository
) : TaskDetailSource {
    override fun observeTask(taskId: Long): Flow<Task?> =
        repository.observeTaskById(taskId)

    override suspend fun getTask(taskId: Long): Task? =
        repository.getTaskById(taskId)

    override suspend fun updateTask(task: Task) = repository.update(task)

    override suspend fun deleteTask(task: Task) = repository.delete(task)
}
