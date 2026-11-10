package com.example.taskflow.ui.home

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface HomeTaskSource {

    fun observeTasks(): Flow<List<HomeTaskUiModel>>

    fun observeTask(taskId: Long): Flow<HomeTaskUiModel?> {
        return observeTasks().map { tasks ->
            tasks.firstOrNull { it.id == taskId }
        }
    }

    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean)
}
