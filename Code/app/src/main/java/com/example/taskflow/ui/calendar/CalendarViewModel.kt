package com.example.taskflow.ui.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskflow.ui.home.HomeTaskSource
import com.example.taskflow.ui.home.HomeTaskUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val taskSource: HomeTaskSource,
    private val savedStateHandle: SavedStateHandle,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val today: () -> LocalDate = { LocalDate.now(zoneId) }
) : ViewModel() {
    private val initialDate = today()
    private val selectedDateText = savedStateHandle.getStateFlow(
        SELECTED_DATE_KEY,
        initialDate.toString()
    )
    private val retrySignal = MutableStateFlow(0)

    private val loadState: Flow<CalendarLoadState> = retrySignal.flatMapLatest {
        flow {
            emit(CalendarLoadState(isLoading = true))
            emitAll(taskSource.observeTasks().map { tasks ->
                CalendarLoadState(tasks = tasks, isLoading = false)
            })
        }.catch {
            emit(CalendarLoadState(isLoading = false, hasError = true))
        }
    }

    val uiState: StateFlow<CalendarUiState> = combine(
        selectedDateText,
        loadState
    ) { dateText, load ->
        CalendarDateHelper.buildUiState(
            selectedDate = LocalDate.parse(dateText),
            tasks = load.tasks,
            zoneId = zoneId,
            today = today(),
            isLoading = load.isLoading,
            hasError = load.hasError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarDateHelper.buildUiState(
            selectedDate = LocalDate.parse(selectedDateText.value),
            tasks = emptyList(),
            zoneId = zoneId,
            today = initialDate,
            isLoading = true
        )
    )

    fun selectDate(date: LocalDate) {
        savedStateHandle[SELECTED_DATE_KEY] = date.toString()
    }

    fun previousMonth() = changeMonth(-1L)

    fun nextMonth() = changeMonth(1L)

    fun selectToday() = selectDate(today())

    fun retry() {
        retrySignal.update { it + 1 }
    }

    private fun changeMonth(offset: Long) {
        // LocalDate tự điều chỉnh ngày 29/30/31 về ngày hợp lệ của tháng mới.
        selectDate(LocalDate.parse(selectedDateText.value).plusMonths(offset))
    }

    private data class CalendarLoadState(
        val tasks: List<HomeTaskUiModel> = emptyList(),
        val isLoading: Boolean = true,
        val hasError: Boolean = false
    )

    private companion object {
        const val SELECTED_DATE_KEY = "calendar_selected_date"
    }
}
