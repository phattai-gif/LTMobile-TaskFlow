package com.example.taskflow.ui.navigation

import com.example.taskflow.R
import com.example.taskflow.ui.components.TaskFlowNavItem

val taskFlowMainDestinations = listOf(
    TaskFlowNavItem(
        id = TaskFlowRoutes.HOME,
        labelRes = R.string.nav_home,
        iconRes = R.drawable.ic_nav_home
    ),
    TaskFlowNavItem(
        id = TaskFlowRoutes.CALENDAR,
        labelRes = R.string.nav_calendar,
        iconRes = R.drawable.ic_nav_calendar
    ),
    TaskFlowNavItem(
        id = TaskFlowRoutes.STATS,
        labelRes = R.string.nav_stats,
        iconRes = R.drawable.ic_nav_stats
    ),
    TaskFlowNavItem(
        id = TaskFlowRoutes.STREAK,
        labelRes = R.string.nav_streak,
        iconRes = R.drawable.ic_nav_streak
    ),
    TaskFlowNavItem(
        id = TaskFlowRoutes.REWARDS,
        labelRes = R.string.nav_rewards,
        iconRes = R.drawable.ic_nav_rewards
    ),
    TaskFlowNavItem(
        id = TaskFlowRoutes.SETTINGS,
        labelRes = R.string.nav_settings,
        iconRes = R.drawable.ic_nav_settings
    )
)