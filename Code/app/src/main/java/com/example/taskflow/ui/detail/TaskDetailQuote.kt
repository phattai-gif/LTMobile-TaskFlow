package com.example.taskflow.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.taskflow.ui.theme.TaskFlowDimensions
import com.team.taskmanagementapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

/** Không giữ Handler/callback toàn cục: việc hủy collector sẽ dừng vòng lặp. */
internal fun quoteTicks(): Flow<Unit> = flow {
    while (true) {
        delay(5_000L)
        emit(Unit)
    }
}

@Composable
fun TaskDetailQuote(taskId: Int, modifier: Modifier = Modifier) {
    val quotes = listOf(
        R.string.task_detail_quote_1,
        R.string.task_detail_quote_2,
        R.string.task_detail_quote_3,
        R.string.task_detail_quote_4
    )
    var quoteIndex by rememberSaveable(taskId) { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(taskId, lifecycleOwner) {
        // Dừng khi rời màn hình, mở màn hình khác hoặc đưa ứng dụng xuống nền.
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            quoteTicks().collect {
                quoteIndex = (quoteIndex + 1) % quotes.size
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(
            modifier = Modifier.padding(TaskFlowDimensions.cardPadding),
            verticalArrangement = Arrangement.spacedBy(TaskFlowDimensions.spacing12)
        ) {
            Text(
                text = stringResource(R.string.task_detail_motivation),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = stringResource(quotes[quoteIndex]),
                modifier = Modifier.testTag("task_detail_quote"),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
