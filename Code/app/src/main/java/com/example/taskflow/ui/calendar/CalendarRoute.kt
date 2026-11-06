package com.example.taskflow.ui.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskflow.ui.home.HomeTaskSource

@Composable
fun CalendarRoute(
    taskSource: HomeTaskSource,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendarViewModel = viewModel<CalendarViewModel> {
        CalendarViewModel(
            taskSource = taskSource,
            savedStateHandle = createSavedStateHandle()
        )
    }
    val uiState by calendarViewModel.uiState.collectAsStateWithLifecycle()

    CalendarContent(
        uiState = uiState,
        onPreviousMonth = calendarViewModel::previousMonth,
        onNextMonth = calendarViewModel::nextMonth,
        onToday = calendarViewModel::selectToday,
        onDateSelected = calendarViewModel::selectDate,
        onTaskClick = onTaskClick,
        onRetry = calendarViewModel::retry,
        modifier = modifier
    )
}
