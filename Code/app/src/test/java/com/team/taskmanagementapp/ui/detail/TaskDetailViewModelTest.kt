package com.team.taskmanagementapp.ui.detail

import androidx.lifecycle.ViewModelStore
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.data.model.enums.RecurrenceType
import com.team.taskmanagementapp.data.model.enums.TaskStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val timestamp = 123_456L
    private val sample = Task(
        id = 17,
        title = "Chuẩn bị thuyết trình",
        description = "Nội dung cần chuẩn bị",
        dueDate = 0L,
        dueTime = 0L,
        priority = Priority.HIGH
    )

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() {
        store.clear()
        dispatcher.scheduler.runCurrent()
        Dispatchers.resetMain()
    }

    private fun createModel(source: FakeSource): TaskDetailViewModel =
        TaskDetailViewModel(17L, source) { timestamp }.also { store.put("detail", it) }

    @Test
    fun observesSelectedTaskAndChangesFromOtherScreens() = runTest(dispatcher) {
        val source = FakeSource(sample)
        val model = createModel(source)
        runCurrent()
        assertEquals(sample, model.uiState.value.task)
        assertFalse(model.uiState.value.isLoading)

        source.stored.value = sample.copy(description = "Đã đổi ở màn hình khác")
        runCurrent()
        assertEquals("Đã đổi ở màn hình khác", model.uiState.value.task?.description)
    }

    @Test
    fun completesThenClearsCompletionDateWhenReopened() = runTest(dispatcher) {
        val source = FakeSource(sample)
        val model = createModel(source)
        runCurrent()
        model.toggleComplete()
        runCurrent()
        assertTrue(source.stored.value!!.isCompleted)
        assertEquals(TaskStatus.COMPLETED, source.stored.value?.status)
        assertEquals(timestamp, source.stored.value?.completedAt)

        model.toggleComplete()
        runCurrent()
        assertFalse(source.stored.value!!.isCompleted)
        assertEquals(TaskStatus.TODO, source.stored.value?.status)
        assertNull(source.stored.value?.completedAt)
        assertEquals(2, source.completionCalls)
    }

    @Test
    fun failedCompletionLeavesTaskUnchangedAndCanBeRetried() = runTest(dispatcher) {
        val source = FakeSource(sample)
        source.failCompletion = true
        val model = createModel(source)
        runCurrent()
        model.toggleComplete()
        runCurrent()
        assertEquals(sample, source.stored.value)
        assertEquals(TaskDetailActionError.COMPLETE, model.uiState.value.actionError)
        assertFalse(model.uiState.value.isBusy)

        source.failCompletion = false
        model.toggleComplete()
        runCurrent()
        assertTrue(source.stored.value!!.isCompleted)
        assertNull(model.uiState.value.actionError)
    }

    @Test
    fun quickEditPreservesLatestRecurrenceAndCompletionHistory() = runTest(dispatcher) {
        val source = FakeSource(sample)
        val model = createModel(source)
        runCurrent()
        // Cập nhật dữ liệu ngay trước khi bấm Lưu, chưa cho collector chạy lại.
        source.stored.value = sample.copy(
            isRecurring = true,
            recurrenceType = RecurrenceType.WEEKLY,
            isCompleted = true,
            status = TaskStatus.COMPLETED,
            completedAt = 99L,
            totalFocusTimeMinutes = 75
        )
        model.saveContent("  Tiêu đề mới  ", "Mô tả mới")
        runCurrent()
        val saved = source.stored.value!!
        assertEquals("Tiêu đề mới", saved.title)
        assertEquals(RecurrenceType.WEEKLY, saved.recurrenceType)
        assertEquals(99L, saved.completedAt)
        assertEquals(75, saved.totalFocusTimeMinutes)
        assertTrue(model.uiState.value.editSaved)
    }

    @Test
    fun repeatedTapsDoNotRunTwoUpdates() = runTest(dispatcher) {
        val source = FakeSource(sample)
        source.updateGate = CompletableDeferred()
        val model = createModel(source)
        runCurrent()
        model.toggleComplete()
        model.toggleComplete()
        runCurrent()
        assertEquals(1, source.updateCalls)
        assertTrue(model.uiState.value.isBusy)
        source.updateGate!!.complete(Unit)
        runCurrent()
        assertTrue(source.stored.value!!.isCompleted)
        assertFalse(model.uiState.value.isBusy)
    }

    @Test
    fun failedDeleteStaysOnScreenAndCanBeRetried() = runTest(dispatcher) {
        val source = FakeSource(sample)
        source.failDelete = true
        val model = createModel(source)
        runCurrent()
        model.deleteTask()
        runCurrent()
        assertFalse(model.uiState.value.isDeleted)
        assertEquals(TaskDetailActionError.DELETE, model.uiState.value.actionError)
        assertEquals(sample, source.stored.value)

        source.failDelete = false
        model.deleteTask()
        runCurrent()
        assertTrue(model.uiState.value.isDeleted)
        assertNull(source.stored.value)
    }

    @Test
    fun failedObservationCanBeRetriedAndMissingTaskIsHandled() = runTest(dispatcher) {
        val source = FakeSource(sample)
        source.failObserve = true
        val model = createModel(source)
        runCurrent()
        assertTrue(model.uiState.value.hasLoadError)
        source.failObserve = false
        model.retry()
        runCurrent()
        assertFalse(model.uiState.value.hasLoadError)
        assertEquals(sample, model.uiState.value.task)
        source.stored.value = null
        runCurrent()
        assertNull(model.uiState.value.task)
        assertFalse(model.uiState.value.isLoading)
    }

    @Test
    fun blankTitleDoesNotModifyStoredTask() = runTest(dispatcher) {
        val source = FakeSource(sample)
        val model = createModel(source)
        runCurrent()
        model.saveContent("  ", "Nội dung thay đổi")
        runCurrent()
        assertEquals(0, source.updateCalls)
        assertEquals(sample, source.stored.value)
    }

    @Test
    fun quoteTicksEveryFiveSecondsAndStopsWhenCancelled() = runTest(dispatcher) {
        var ticks = 0
        val job = launch { quoteTicks().collect { ticks++ } }
        runCurrent()
        advanceTimeBy(4_999L)
        runCurrent()
        assertEquals(0, ticks)
        advanceTimeBy(1L)
        runCurrent()
        assertEquals(1, ticks)
        advanceTimeBy(5_000L)
        runCurrent()
        assertEquals(2, ticks)
        job.cancelAndJoin()
        advanceTimeBy(50_000L)
        runCurrent()
        assertEquals(2, ticks)
    }

    private class FakeSource(task: Task?) : TaskDetailSource {
        val stored = MutableStateFlow(task)
        var failObserve = false
        var failDelete = false
        var failCompletion = false
        var completionCalls = 0
        var updateCalls = 0
        var updateGate: CompletableDeferred<Unit>? = null

        override fun observeTask(taskId: Long): Flow<Task?> = flow {
            if (failObserve) error("Không đọc được dữ liệu")
            emitAll(stored)
        }

        override suspend fun getTask(taskId: Long): Task? = stored.value

        override suspend fun updateTask(task: Task) {
            updateCalls++
            updateGate?.await()
            stored.value = task
        }

        override suspend fun updateCompletion(previous: Task, updated: Task) {
            completionCalls++
            if (failCompletion) error("Không cập nhật được kỳ lặp")
            updateTask(updated)
        }

        override suspend fun deleteTask(task: Task) {
            if (failDelete) error("Không xóa được dữ liệu")
            stored.value = null
        }
    }
}
