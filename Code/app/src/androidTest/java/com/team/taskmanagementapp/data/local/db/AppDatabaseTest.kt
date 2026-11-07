package com.team.taskmanagementapp.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.team.taskmanagementapp.data.local.entity.PomodoroSession
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.SessionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class AppDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun createDatabase() {
        val context: Context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun taskCrudAndPomodoroCascadeWork() = runBlocking {
        val now = System.currentTimeMillis()

        // 1. Tạo task và kiểm tra Room lưu/đọc
        val taskId = database.taskDao().insertTask(
            Task(
                id = 0,
                title = "Test Room",
                description = "Kiểm tra lưu dữ liệu",
                dueDate = now,
                dueTime = now,
                priority = Priority.MEDIUM
            )
        )

        val savedTask = database.taskDao().getTaskById(taskId)
        assertNotNull(savedTask)
        assertEquals("Test Room", savedTask?.title)
        assertEquals(1, database.taskDao().getAllTasks().first().size)

        // 2. Cập nhật task
        val updatedTask = savedTask!!.copy(title = "Task đã cập nhật")
        database.taskDao().updateTask(updatedTask)

        assertEquals(
            "Task đã cập nhật",
            database.taskDao().getTaskById(taskId)?.title
        )

        // 3. Lưu phiên Pomodoro gắn với task
        val endTimeCalc = now + (25 * 60 * 1000L)
        database.pomodoroDao().insertSession(
            PomodoroSession(
                id = 0,
                taskId = taskId,
                startTime = now,
                endTime = endTimeCalc,
                durationInMinutes = 25,
                sessionType = SessionType.FOCUS,
                isCompleted = true
            )
        )

        assertEquals(
            1,
            database.pomodoroDao().getSessionsByTaskId(taskId).size
        )

        // 4. Xóa task: kiểm tra CASCADE tự xóa PomodoroSession liên quan
        database.taskDao().deleteTask(updatedTask)

        assertNull(database.taskDao().getTaskById(taskId))
        assertTrue(database.pomodoroDao().getSessionsByTaskId(taskId).isEmpty())
    }
}