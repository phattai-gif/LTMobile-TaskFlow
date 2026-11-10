package com.team.taskmanagementapp.ui.home

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data object Empty : HomeUiState

    data object Error : HomeUiState

    data class Success(
        val todayTasks: List<HomeTaskUiModel>,
        val upcomingTasks: List<HomeTaskUiModel>
    ) : HomeUiState
}
