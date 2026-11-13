package com.team.taskmanagementapp.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeRoute(
    taskSource: HomeTaskSource,
    onTaskClick: (Long) -> Unit,
    onViewCalendarClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onOpenPomodoro: () -> Unit = {}
) {
    val homeViewModel = viewModel<HomeViewModel> {
        HomeViewModel(taskSource)
    }

    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onTaskClick = onTaskClick,
        onToggleComplete = homeViewModel::toggleTaskComplete,
        onDeleteTask = { homeViewModel.deleteTask(it) },
        onRetry = homeViewModel::retry,
        onViewCalendarClick = onViewCalendarClick,
        onClockClick = onOpenPomodoro,
        modifier = modifier
    )
}
