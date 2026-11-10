package com.example.taskflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.team.taskmanagementapp.R
import com.example.taskflow.ui.components.TaskFlowButton
import com.example.taskflow.ui.components.TaskFlowTextField
import com.example.taskflow.ui.theme.TaskFlowDimensions

@Composable
fun CreateTaskScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(TaskFlowDimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(
            TaskFlowDimensions.spacing16
        )
    ) {
        Text(
            text = stringResource(R.string.action_create_task),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        TaskFlowTextField(
            value = title,
            onValueChange = { title = it },
            label = stringResource(R.string.task_title_label),
            modifier = Modifier.fillMaxWidth()
        )

        TaskFlowTextField(
            value = description,
            onValueChange = { description = it },
            label = stringResource(R.string.task_description_label),
            modifier = Modifier.fillMaxWidth(),
            singleLine = false
        )

        TaskFlowButton(
            text = stringResource(R.string.action_cancel),
            onClick = {
                focusManager.clearFocus()
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}