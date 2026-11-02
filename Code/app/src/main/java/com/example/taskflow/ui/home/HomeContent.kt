package com.example.taskflow.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.taskflow.R
import com.example.taskflow.ui.components.TaskFlowButton
import com.example.taskflow.ui.theme.TaskFlowDimensions

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onTaskClick: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        HomeUiState.Loading -> {
            HomeStateMessage(
                messageRes = R.string.home_loading,
                modifier = modifier,
                showProgress = true
            )
        }

        HomeUiState.Empty -> {
            HomeStateMessage(
                messageRes = R.string.home_empty,
                modifier = modifier
            )
        }

        HomeUiState.Error -> {
            HomeStateMessage(
                messageRes = R.string.home_load_error,
                modifier = modifier,
                onRetry = onRetry
            )
        }

        is HomeUiState.Success -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    TaskFlowDimensions.screenPadding
                ),
                verticalArrangement = Arrangement.spacedBy(
                    TaskFlowDimensions.spacing12
                )
            ) {
                item(key = "home_title") {
                    Text(
                        text = stringResource(R.string.nav_home),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item(key = "today_header") {
                    Text(
                        text = stringResource(R.string.home_today_tasks),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (uiState.todayTasks.isEmpty()) {
                    item(key = "today_empty") {
                        Text(
                            text = stringResource(
                                R.string.home_today_empty
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(
                    items = uiState.todayTasks,
                    key = { "today_${it.id}" }
                ) { task ->
                    HomeTaskCard(
                        task = task,
                        onTaskClick = onTaskClick
                    )
                }

                item(key = "upcoming_header") {
                    Text(
                        text = stringResource(
                            R.string.home_upcoming_tasks
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (uiState.upcomingTasks.isEmpty()) {
                    item(key = "upcoming_empty") {
                        Text(
                            text = stringResource(
                                R.string.home_upcoming_empty
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(
                    items = uiState.upcomingTasks,
                    key = { "upcoming_${it.id}" }
                ) { task ->
                    HomeTaskCard(
                        task = task,
                        onTaskClick = onTaskClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeStateMessage(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(TaskFlowDimensions.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            TaskFlowDimensions.spacing16,
            Alignment.CenterVertically
        )
    ) {
        if (showProgress) {
            CircularProgressIndicator()
        }

        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        if (onRetry != null) {
            TaskFlowButton(
                text = stringResource(R.string.action_retry),
                onClick = onRetry
            )
        }
    }
}