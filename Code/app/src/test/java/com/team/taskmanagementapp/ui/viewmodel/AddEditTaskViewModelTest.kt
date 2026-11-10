package com.team.taskmanagementapp.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.team.taskmanagementapp.data.local.dao.TaskDao
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.RecurrenceType
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import com.team.taskmanagementapp.data.repository.TaskRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditTaskViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeTaskDao
    private lateinit var repository: TaskRepository
    private lateinit var viewModel: AddEditTaskViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeTaskDao()
        repository = TaskRepository(fakeDao)
        viewModel = AddEditTaskViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isIdle() {
        assertEquals(AddEditTaskState.Idle, viewModel.state.value)
    }

    @Test
    fun saveTask_newTask_insertsAndEmitsSaved() = runTest {
        viewModel.saveTask(
            taskId = -1L,
            title = "Task mới",
            description = "Mô tả công việc",
            dueDate = 1731234567000L,
            dueTime = 1731238167000L,
            priority = Priority.HIGH,
            status = TaskStatus.TODO,
            recurrenceType = RecurrenceType.NONE,
            reminderMinutes = 15
        )

        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue("Expected Saved state but got $state", state is AddEditTaskState.Saved)
        val savedTask = (state as AddEditTaskState.Saved).task
        assertEquals("Task mới", savedTask.title)
        assertEquals(Priority.HIGH, savedTask.priority)
        assertEquals(1, fakeDao.tasks.size)
    }

    @Test
    fun loadTask_existingTask_emitsLoaded() = runTest {
        val existing = Task(
            id = 42,
            title = "Task có sẵn",
            description = "Chi tiết",
            dueDate = 1731234567000L,
            dueTime = 0L,
            priority = Priority.MEDIUM,
            status = TaskStatus.TODO
        )
        fakeDao.tasks[42] = existing

        viewModel.loadTask(42L)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue("Expected Loaded state but got $state", state is AddEditTaskState.Loaded)
        assertEquals("Task có sẵn", (state as AddEditTaskState.Loaded).task.title)
    }

    @Test
    fun loadTask_nonExistentTask_emitsError() = runTest {
        viewModel.loadTask(999L)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue("Expected Error state but got $state", state is AddEditTaskState.Error)
    }

    @Test
    fun saveTask_existingTask_updatesAndEmitsSaved() = runTest {
        val existing = Task(
            id = 10,
            title = "Tên cũ",
            description = "Mô tả cũ",
            dueDate = 1000L,
            dueTime = 0L,
            priority = Priority.LOW,
            status = TaskStatus.TODO
        )
        fakeDao.tasks[10] = existing

        viewModel.saveTask(
            taskId = 10L,
            title = "Tên mới đã sửa",
            description = "Mô tả mới",
            dueDate = 2000L,
            dueTime = 0L,
            priority = Priority.HIGH,
            status = TaskStatus.COMPLETED,
            recurrenceType = RecurrenceType.NONE,
            reminderMinutes = 0
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue("Expected Saved state but got $state", state is AddEditTaskState.Saved)
        val updatedTask = (state as AddEditTaskState.Saved).task
        assertEquals("Tên mới đã sửa", updatedTask.title)
        assertEquals(TaskStatus.COMPLETED, updatedTask.status)
        assertTrue(updatedTask.isCompleted)
        assertEquals("Tên mới đã sửa", fakeDao.tasks[10]?.title)
    }

    private class FakeTaskDao : TaskDao {
        val tasks = mutableMapOf<Int, Task>()
        private var autoId = 1

        override suspend fun insertTask(task: Task): Long {
            val id = if (task.id > 0) task.id else autoId++
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
