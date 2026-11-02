package com.team.taskmanagementapp.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.FilterCriteria
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = TaskRepository(database.taskDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun repositoryCanCreateReadUpdateDeleteAndFilterTask() = runBlocking {
        val tomorrow = System.currentTimeMillis() + 24 * 60 * 60 * 1000L

        val taskId = repository.insert(
            Task(
                title = "Kiểm tra Repository",
                description = "Test thêm, đọc, sửa, xóa và lọc",
                dueDate = tomorrow,
                dueTime = tomorrow,
                priority = Priority.HIGH
            )
        )

        // Read: kiểm tra task đã được lưu.
        val savedTask = repository.getTaskById(taskId)
        assertNotNull(savedTask)
        assertEquals("Kiểm tra Repository", savedTask?.title)

        // Flow: kiểm tra danh sách được phát ra từ Room.
        val allTasks = repository.getAllTasks().first()
        assertEquals(1, allTasks.size)

        // Filter: lấy task theo trạng thái và độ ưu tiên.
        val filteredTasks = repository.getFilteredTasks(
            FilterCriteria(
                statuses = setOf(TaskStatus.TODO),
                priorities = setOf(Priority.HIGH)
            )
        ).first()

        assertEquals(1, filteredTasks.size)
        assertEquals(taskId.toInt(), filteredTasks.first().id)

        // Update: sửa tiêu đề task.
        val updatedTask = savedTask!!.copy(title = "Repository đã cập nhật")
        repository.update(updatedTask)

        assertEquals(
            "Repository đã cập nhật",
            repository.getTaskById(taskId)?.title
        )

        // Delete: xóa task và xác nhận không còn trong database.
        repository.delete(updatedTask)
        assertNull(repository.getTaskById(taskId))
        assertEquals(0, repository.getAllTasks().first().size)
    }

    @Test
    fun repositoryMarksPastTaskAsOverdue() = runBlocking {
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000L

        val taskId = repository.insert(
            Task(
                title = "Task quá hạn",
                description = "Dùng để kiểm tra cập nhật trạng thái",
                dueDate = yesterday,
                dueTime = yesterday,
                priority = Priority.MEDIUM
            )
        )

        repository.checkAndUpdateOverdueTasks()

        assertEquals(
            TaskStatus.OVERDUE,
            repository.getTaskById(taskId)?.status
        )
    }
}