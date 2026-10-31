package com.example.taskflow.ui.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.taskflow.R
import com.example.taskflow.ui.components.TaskFlowBottomNavigation
import com.example.taskflow.ui.components.TaskFlowButton
import com.example.taskflow.ui.components.TaskFlowCard
import com.example.taskflow.ui.components.TaskFlowConfirmDialog
import com.example.taskflow.ui.components.TaskFlowNavItem
import com.example.taskflow.ui.components.TaskFlowTextField
import com.example.taskflow.ui.theme.TaskFLowTheme
import com.example.taskflow.ui.theme.TaskFlowDimensions

@Composable
fun ThemeDemoScreen(
    modifier: Modifier = Modifier
) {
    var selectedItemId by rememberSaveable {
        mutableStateOf("tasks")
    }
    var taskName by rememberSaveable {
        mutableStateOf("")
    }
    var showError by rememberSaveable {
        mutableStateOf(false)
    }
    var showDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var confirmed by rememberSaveable {
        mutableStateOf(false)
    }

    val focusManager = LocalFocusManager.current

    val navigationItems = listOf(
        TaskFlowNavItem(
            id = "tasks",
            labelRes = R.string.nav_tasks,
            iconRes = R.drawable.ic_nav_home
        ),
        TaskFlowNavItem(
            id = "calendar",
            labelRes = R.string.nav_calendar,
            iconRes = R.drawable.ic_nav_calendar
        ),
        TaskFlowNavItem(
            id = "settings",
            labelRes = R.string.nav_settings,
            iconRes = R.drawable.ic_nav_settings
        )
    )

    val selectedItem = navigationItems.first {
        it.id == selectedItemId
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            TaskFlowBottomNavigation(
                items = navigationItems,
                selectedItemId = selectedItemId,
                onItemSelected = {
                    selectedItemId = it
                    confirmed = false
                    focusManager.clearFocus()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(TaskFlowDimensions.screenPadding),
            verticalArrangement = Arrangement.spacedBy(
                TaskFlowDimensions.spacing16
            )
        ) {
            Text(
                text = stringResource(R.string.theme_demo_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            TaskFlowCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(selectedItem.labelRes),
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = stringResource(
                        R.string.theme_demo_description
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )

                if (selectedItemId == "tasks") {
                    TaskFlowTextField(
                        value = taskName,
                        onValueChange = {
                            taskName = it
                            showError = false
                            confirmed = false
                        },
                        label = stringResource(
                            R.string.theme_demo_input_label
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        errorMessage = if (showError) {
                            stringResource(
                                R.string.theme_demo_input_error
                            )
                        } else {
                            null
                        }
                    )

                    TaskFlowButton(
                        text = stringResource(
                            R.string.theme_demo_open_dialog
                        ),
                        onClick = {
                            showError = taskName.isBlank()

                            if (!showError) {
                                confirmed = false
                                focusManager.clearFocus()
                                showDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    TaskFlowButton(
                        text = stringResource(
                            R.string.theme_demo_disabled_button
                        ),
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }
            }

            if (confirmed) {
                Text(
                    text = stringResource(
                        R.string.theme_demo_confirmed
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showDialog) {
        TaskFlowConfirmDialog(
            title = stringResource(
                R.string.theme_demo_dialog_title
            ),
            message = stringResource(
                R.string.theme_demo_dialog_message
            ),
            onConfirm = {
                showDialog = false
                confirmed = true
            },
            onDismiss = {
                showDialog = false
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeDemoPreview() {
    TaskFLowTheme {
        ThemeDemoScreen()
    }
}