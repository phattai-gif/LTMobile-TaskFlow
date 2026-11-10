package com.team.taskmanagementapp.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.ui.components.TaskFlowBottomNavigation
import com.team.taskmanagementapp.ui.navigation.TaskFlowRoutes
import com.team.taskmanagementapp.ui.navigation.taskFlowMainDestinations
import com.team.taskmanagementapp.ui.calendar.CalendarRoute
import com.team.taskmanagementapp.ui.screens.RewardsScreen
import com.team.taskmanagementapp.ui.screens.SettingsScreen
import com.team.taskmanagementapp.ui.screens.StatsScreen
import com.team.taskmanagementapp.ui.screens.StreakScreen
import com.team.taskmanagementapp.ui.theme.TaskFlowDimensions
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.team.taskmanagementapp.ui.home.HomeRoute
import com.team.taskmanagementapp.ui.home.RepositoryHomeTaskSource
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.data.repository.TaskRepository
import com.team.taskmanagementapp.ui.detail.RepositoryTaskDetailSource
import com.team.taskmanagementapp.ui.detail.TaskDetailRoute
@Composable
fun TaskFlowApp(
    onCreateTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val appContext = LocalContext.current.applicationContext
    val database = remember(appContext) { AppDatabase.getInstance(appContext) }
    val repository = remember(database) {
        TaskRepository(database.taskDao())
    }

    val taskSource = remember(repository) {
        RepositoryHomeTaskSource(repository)
    }

    val detailSource = remember(repository, database) {
        RepositoryTaskDetailSource(repository, database)
    }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = backStackEntry?.destination?.route
        ?: TaskFlowRoutes.HOME

    val showMainControls = taskFlowMainDestinations.any {
        it.id == currentRoute
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showMainControls) {
                TaskFlowBottomNavigation(
                    items = taskFlowMainDestinations,
                    selectedItemId = currentRoute,
                    onItemSelected = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(
                                    navController.graph
                                        .findStartDestination().id
                                ) {
                                    saveState = true
                                }

                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showMainControls) {
                FloatingActionButton(
                    onClick = onCreateTask,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(
                            R.string.action_create_task
                        ),
                        modifier = Modifier.size(
                            TaskFlowDimensions.iconMedium
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TaskFlowRoutes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(TaskFlowRoutes.HOME) {
                HomeRoute(
                    taskSource = taskSource,
                    onTaskClick = { taskId ->
                        navController.navigate(
                            TaskFlowRoutes.taskDetail(taskId)
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = TaskFlowRoutes.TASK_DETAIL,
                arguments = listOf(
                    navArgument(TaskFlowRoutes.TASK_ID) {
                        type = NavType.LongType
                    }
                )
            ) { entry ->
                TaskDetailRoute(
                    taskId = entry.arguments
                        ?.getLong(TaskFlowRoutes.TASK_ID)
                        ?: -1L,
                    taskSource = detailSource,
                    onEditTask = onEditTask,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(TaskFlowRoutes.CALENDAR) {
                CalendarRoute(
                    taskSource = taskSource,
                    onTaskClick = { taskId ->
                        navController.navigate(TaskFlowRoutes.taskDetail(taskId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(TaskFlowRoutes.STATS) {
                StatsScreen()
            }

            composable(TaskFlowRoutes.STREAK) {
                StreakScreen()
            }

            composable(TaskFlowRoutes.REWARDS) {
                RewardsScreen()
            }

            composable(TaskFlowRoutes.SETTINGS) {
                SettingsScreen()
            }

        }
    }
}
