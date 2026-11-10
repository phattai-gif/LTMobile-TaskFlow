package com.example.taskflow.ui.home

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data object Empty : HomeUiState

    data object Error : HomeUiState

    data class Success(
        val todayTasks: List<HomeTaskUiModel>,
        val upcomingTasks: List<HomeTaskUiModel>
    ) : HomeUiState
}