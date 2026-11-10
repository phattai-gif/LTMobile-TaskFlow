package com.team.taskmanagementapp.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.taskflow.ui.home.RepositoryHomeTaskSource
import com.team.taskmanagementapp.data.local.dao.TaskDao
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.RecurrenceType
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.data.repository.TaskRepository
import com.team.taskmanagementapp.viewmodel.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskDeleteTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeTaskDao
    private lateinit var repository: TaskRepository
    private lateinit var viewModel: TaskViewModel
    private lateinit var homeSource: RepositoryHomeTaskSource

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeTaskDao()
        repository = TaskRepository(fakeDao)
        viewModel = TaskViewModel(repository)
        homeSource = RepositoryHomeTaskSource(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun deleteTask_viaViewModel_removesTaskFromDatabase() = runTest {
        val task = Task(
            id = 10,
            title = "Task To Delete",
            description = "Description",
            dueDate = System.currentTimeMillis() + 3600_000L,
            dueTime = 0L,
            priority = Priority.HIGH,
            status = TaskStatus.TODO,
            isCompleted = false,
            completedAt = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        fakeDao.insertTask(task)
        assertNotNull(repository.getTaskById(10L))

        viewModel.deleteTask(task)
        advanceUntilIdle()

        val deleted = repository.getTaskById(10L)
        assertNull(deleted)
    }

    @Test
    fun deleteTask_viaHomeSource_removesTaskFromDatabase() = runTest {
        val task = Task(
            id = 20,
            title = "Home Source Delete Task",
            description = "Description",
            dueDate = System.currentTimeMillis() + 3600_000L,
            dueTime = 0L,
            priority = Priority.MEDIUM,
            status = TaskStatus.TODO,
            isCompleted = false,
            completedAt = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        fakeDao.insertTask(task)
        assertNotNull(repository.getTaskById(20L))

        homeSource.deleteTask(20L)

        val deleted = repository.getTaskById(20L)
        assertNull(deleted)
    }

    private class FakeTaskDao : TaskDao {
        val tasks = mutableMapOf<Int, Task>()
        private var idCounter = 1

        override suspend fun insertTask(task: Task): Long {
            val id = if (task.id > 0) task.id else idCounter++
            tasks[id] = task.copy(id = id)
            return id.toLong()
        }

        override suspend fun updateTask(task: Task) {
            tasks[task.id] = task
        }

        override suspend fun deleteTask(task: Task) {
            tasks.remove(task.id)
        }

        override fun getAllTasks(): Flow<List<Task>> = flowOf(tasks.values.toList())
        override suspend fun getActiveTasksSync(): List<Task> = tasks.values.filter { !it.isCompleted }
        override suspend fun getAllTasksSync(): List<Task> = tasks.values.toList()
        override suspend fun getCompletedTasksCount(): Int = tasks.values.count { it.isCompleted }
        override suspend fun insertAllTasks(taskList: List<Task>) {
            taskList.forEach { insertTask(it) }
        }
        override suspend fun getTaskById(taskId: Long): Task? = tasks[taskId.toInt()]
        override fun observeTaskById(taskId: Long): Flow<Task?> = flowOf(tasks[taskId.toInt()])
        override fun searchTasksByTitle(query: String): Flow<List<Task>> =
            flowOf(tasks.values.filter { it.title.contains(query, ignoreCase = true) })
        override fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> =
            flowOf(tasks.values.filter { it.status == status })
        override fun getTasksByPriority(priority: Priority): Flow<List<Task>> =
            flowOf(tasks.values.filter { it.priority == priority })
        override fun getTasksByDateRange(startDate: Long, endDate: Long): Flow<List<Task>> =
            flowOf(tasks.values.filter { it.dueDate in startDate..endDate })
        override fun getOverdueTasks(currentTime: Long): Flow<List<Task>> =
            flowOf(tasks.values.filter { it.dueDate < currentTime && !it.isCompleted })
        override suspend fun getOverdueTasksSync(currentTime: Long): List<Task> = emptyList()
        override suspend fun deleteAllTasks() { tasks.clear() }
        override suspend fun markOverdueTasks(currentTime: Long) {}
        override suspend fun getFutureRecurringTasksSync(
            title: String,
            recurrenceType: RecurrenceType,
            startDate: Long
        ): List<Task> = emptyList()
        override suspend fun getUncompletedTasksByTitleSync(
            title: String,
            excludeTaskId: Long
        ): List<Task> = emptyList()
        override fun getFilteredTasks(
            status: TaskStatus?,
            priority: Priority?,
            startDate: Long?,
            endDate: Long?,
            isOverdueOnly: Int,
            currentTime: Long
        ): Flow<List<Task>> = flowOf(tasks.values.toList())

        override suspend fun deleteFutureRecurringTasks(
            title: String,
            recurrenceType: RecurrenceType,
            startDate: Long
        ) {}

        override suspend fun insertBatch(taskList: List<Task>): List<Long> =
            taskList.map { insertTask(it) }

        override suspend fun getConflictingTask(
            title: String,
            dueDate: Long,
            recurrenceType: RecurrenceType
        ): Task? = null

        override suspend fun getTasksForDateRangeSync(
            startMillis: Long,
            endMillis: Long
        ): List<Task> = emptyList()

        override suspend fun updateFutureRecurringTasks(
            originalTitle: String,
            originalRecurrence: RecurrenceType,
            startDate: Long,
            newTitle: String,
            newDescription: String,
            newPriority: Priority,
            newRecurrenceType: RecurrenceType,
            newReminderMinutes: Int,
            updatedAt: Long
        ) {}
    }
}
