package com.team.taskmanagementapp.ui

import android.content.Intent
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.ui.components.TaskFlowBottomNavigation
import com.team.taskmanagementapp.ui.navigation.TaskFlowRoutes
import com.team.taskmanagementapp.ui.navigation.taskFlowMainDestinations
import com.team.taskmanagementapp.ui.calendar.CalendarScreen
import com.team.taskmanagementapp.ui.stats.StatsScreen
import com.team.taskmanagementapp.ui.screens.CreateTaskScreen
import com.team.taskmanagementapp.ui.screens.RewardsScreen
import com.team.taskmanagementapp.ui.screens.SettingsScreen
import com.team.taskmanagementapp.ui.screens.StreakScreen
import com.team.taskmanagementapp.ui.theme.TaskFlowDimensions
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.team.taskmanagementapp.ui.activity.AddEditTaskActivity
import com.team.taskmanagementapp.ui.detail.RepositoryTaskDetailSource
import com.team.taskmanagementapp.ui.detail.TaskDetailRoute
import com.team.taskmanagementapp.ui.home.HomeRoute
import com.team.taskmanagementapp.ui.home.RepositoryHomeTaskSource
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.data.repository.TaskRepository

@Composable
fun TaskFlowApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val database = remember(appContext) { AppDatabase.getInstance(appContext) }
    val repository = remember(database) { TaskRepository(database.taskDao()) }
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    },
                    onAddClick = {
                        navController.navigate(TaskFlowRoutes.CREATE_TASK) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showMainControls) {
                // Floating AI Chatbot Avatar at bottom right positioned cleanly above bottom bar
                androidx.compose.material3.Surface(
                    onClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                "AI Assistant is ready to help!"
                            )
                        }
                    },
                    shape = CircleShape,
                    color = Color.Transparent,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .padding(bottom = 16.dp, end = 4.dp)
                        .size(54.dp)
                        .clip(CircleShape)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_chatbot_ai),
                        contentDescription = stringResource(R.string.home_ai_assistant),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
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
                        navController.navigate(TaskFlowRoutes.taskDetail(taskId)) {
                            launchSingleTop = true
                        }
                    },
                    onViewCalendarClick = {
                        navController.navigate(TaskFlowRoutes.CALENDAR) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(TaskFlowRoutes.CALENDAR) {
                CalendarScreen()
            }

            composable(
                route = TaskFlowRoutes.TASK_DETAIL,
                arguments = listOf(navArgument(TaskFlowRoutes.TASK_ID) { type = NavType.LongType })
            ) { entry ->
                TaskDetailRoute(
                    taskId = entry.arguments?.getLong(TaskFlowRoutes.TASK_ID) ?: -1L,
                    taskSource = detailSource,
                    onBack = { navController.popBackStack() },
                    onEditTask = { taskId ->
                        val editor = Intent(context, AddEditTaskActivity::class.java)
                            .putExtra(AddEditTaskActivity.EXTRA_TASK_ID, taskId)
                        context.startActivity(editor)
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

            composable(TaskFlowRoutes.CREATE_TASK) {
                CreateTaskScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
