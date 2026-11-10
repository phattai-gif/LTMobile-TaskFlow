package com.team.taskmanagementapp.util

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringTaskGenerationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeTaskDao
    private lateinit var repository: TaskRepository
    private lateinit var viewModel: TaskViewModel

    private fun millisFromDate(year: Int, month: Int, day: Int): Long {
        return LocalDate.of(year, month, day)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeTaskDao()
        repository = TaskRepository(fakeDao)
        viewModel = TaskViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun completeRecurringTask_generatesNextInstanceWithIncrementedOccurrence() = runTest {
        val startDueDate = millisFromDate(2026, 8, 26)
        val initialTask = Task(
            id = 1,
            title = "Daily Standup",
            description = "Họp nhóm mỗi sáng",
            dueDate = startDueDate,
            dueTime = 0L,
            priority = Priority.HIGH,
            status = TaskStatus.TODO,
            isCompleted = false,
            completedAt = null,
            isRecurring = true,
            recurrenceType = RecurrenceType.DAILY,
            recurrenceInterval = 1,
            repeatLimitCount = 5,
            currentOccurrence = 1
        )
        fakeDao.insertTask(initialTask)

        // Hoàn thành task qua ViewModel
        viewModel.toggleTaskComplete(initialTask)
        advanceUntilIdle()

        // 1. Task ban đầu phải được đánh dấu hoàn thành
        val completedOriginal = repository.getTaskById(1L)
        assertNotNull(completedOriginal)
        assertTrue(completedOriginal!!.isCompleted)
        assertEquals(TaskStatus.COMPLETED, completedOriginal.status)

        // 2. Phải tự động sinh task tiếp theo với ngày đến hạn kế tiếp (+1 ngày) và occurrence = 2
        val activeTasks = fakeDao.getActiveTasksSync()
        assertEquals(1, activeTasks.size)
        val nextTask = activeTasks.first()

        assertEquals("Daily Standup", nextTask.title)
        assertEquals(millisFromDate(2026, 8, 27), nextTask.dueDate)
        assertEquals(2, nextTask.currentOccurrence)
        assertEquals(5, nextTask.repeatLimitCount)
        assertFalse(nextTask.isCompleted)
        assertEquals(TaskStatus.TODO, nextTask.status)
        assertNull(nextTask.completedAt)
    }

    @Test
    fun completeRecurringTask_whenLimitCountReached_doesNotGenerateNextInstance() = runTest {
        val startDueDate = millisFromDate(2026, 8, 26)
        val initialTask = Task(
            id = 2,
            title = "Final Recurring Task",
            description = "Lần lặp cuối cùng",
            dueDate = startDueDate,
            dueTime = 0L,
            priority = Priority.MEDIUM,
            status = TaskStatus.TODO,
            isCompleted = false,
            completedAt = null,
            isRecurring = true,
            recurrenceType = RecurrenceType.DAILY,
            recurrenceInterval = 1,
            repeatLimitCount = 3,
            currentOccurrence = 3 // Đã đạt giới hạn tối đa 3 lần
        )
        fakeDao.insertTask(initialTask)

        viewModel.toggleTaskComplete(initialTask)
        advanceUntilIdle()

        val completedOriginal = repository.getTaskById(2L)
        assertNotNull(completedOriginal)
        assertTrue(completedOriginal!!.isCompleted)

        // Không được sinh thêm task mới
        val activeTasks = fakeDao.getActiveTasksSync()
        assertTrue(activeTasks.isEmpty())
        assertEquals(1, fakeDao.tasks.size)
    }

    @Test
    fun completeRecurringTask_whenRepeatEndDateExceeded_doesNotGenerateNextInstance() = runTest {
        val startDueDate = millisFromDate(2026, 8, 26)
        val initialTask = Task(
            id = 3,
            title = "Expiring Recurring Task",
            description = "Chuỗi lặp sắp hết hạn",
            dueDate = startDueDate,
            dueTime = 0L,
            priority = Priority.LOW,
            status = TaskStatus.TODO,
            isCompleted = false,
            completedAt = null,
            isRecurring = true,
            recurrenceType = RecurrenceType.DAILY,
            recurrenceInterval = 1,
            repeatEndDate = millisFromDate(2026, 8, 26) + 3600_000L, // Hạn chót kết thúc trong ngày
            repeatLimitCount = 0,
            currentOccurrence = 1
        )
        fakeDao.insertTask(initialTask)

        // Kỳ tiếp theo sẽ là 27/08/2026, vượt quá repeatEndDate
        viewModel.toggleTaskComplete(initialTask)
        advanceUntilIdle()

        val completedOriginal = repository.getTaskById(3L)
        assertNotNull(completedOriginal)
        assertTrue(completedOriginal!!.isCompleted)

        // Không sinh thêm task mới
        val activeTasks = fakeDao.getActiveTasksSync()
        assertTrue(activeTasks.isEmpty())
    }

    @Test
    fun completeRecurringTask_whenPaused_doesNotGenerateNextInstance() = runTest {
        val startDueDate = millisFromDate(2026, 8, 26)
        val initialTask = Task(
            id = 4,
            title = "Paused Recurring Task",
            description = "Chuỗi lặp đang tạm dừng",
            dueDate = startDueDate,
            dueTime = 0L,
            priority = Priority.MEDIUM,
            status = TaskStatus.TODO,
            isCompleted = false,
            completedAt = null,
            isRecurring = true,
            recurrenceType = RecurrenceType.WEEKLY,
            recurrenceInterval = 1,
            isPaused = true, // Đang tạm dừng
            currentOccurrence = 1
        )
        fakeDao.insertTask(initialTask)

        viewModel.toggleTaskComplete(initialTask)
        advanceUntilIdle()

        val completedOriginal = repository.getTaskById(4L)
        assertNotNull(completedOriginal)
        assertTrue(completedOriginal!!.isCompleted)

        // Không sinh thêm task mới khi đang pause
        val activeTasks = fakeDao.getActiveTasksSync()
        assertTrue(activeTasks.isEmpty())
    }

    @Test
    fun reopenRecurringTask_cleansUpAutoGeneratedFutureInstances() = runTest {
        val startDueDate = millisFromDate(2026, 8, 26)
        val initialTask = Task(
            id = 5,
            title = "Reopen Test",
            description = "Kiểm tra hủy hoàn thành dọn task con",
            dueDate = startDueDate,
            dueTime = 0L,
            priority = Priority.HIGH,
            status = TaskStatus.TODO,
            isCompleted = false,
            completedAt = null,
            isRecurring = true,
            recurrenceType = RecurrenceType.DAILY,
            recurrenceInterval = 1,
            repeatLimitCount = 5,
            currentOccurrence = 1
        )
        fakeDao.insertTask(initialTask)

        // 1. Hoàn thành task -> sinh ra task kế tiếp id=100
        viewModel.toggleTaskComplete(initialTask)
        advanceUntilIdle()
        assertEquals(2, fakeDao.tasks.size)

        // 2. Mở lại (reopen) task ban đầu
        val completedTask = repository.getTaskById(5L)!!
        viewModel.toggleTaskComplete(completedTask)
        advanceUntilIdle()

        // 3. Task ban đầu trở về TODO, task tương lai tự sinh trước đó bị xóa
        val reopenedTask = repository.getTaskById(5L)
        assertNotNull(reopenedTask)
        assertFalse(reopenedTask!!.isCompleted)
        assertEquals(TaskStatus.TODO, reopenedTask.status)
        assertNull(reopenedTask.completedAt)

        // Task con tự sinh đã được dọn dẹp
        val activeTasks = fakeDao.getActiveTasksSync()
        assertEquals(1, activeTasks.size)
        assertEquals(5, activeTasks.first().id)
    }

    private class FakeTaskDao : TaskDao {
        val tasks = mutableMapOf<Int, Task>()
        private var idCounter = 100

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
        ): List<Task> {
            return tasks.values.filter {
                it.title == title && it.recurrenceType == recurrenceType && it.dueDate >= startDate && !it.isCompleted
            }
        }

        override suspend fun deleteFutureRecurringTasks(
            title: String,
            recurrenceType: RecurrenceType,
            startDate: Long
        ) {
            val toRemove = tasks.values.filter {
                it.title == title && it.recurrenceType == recurrenceType && it.dueDate >= startDate && !it.isCompleted
            }.map { it.id }
            toRemove.forEach { tasks.remove(it) }
        }

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
