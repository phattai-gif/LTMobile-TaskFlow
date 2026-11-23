package com.team.taskmanagementapp.ui.navigation

object TaskFlowRoutes {
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val STATS = "stats"
    const val STREAK = "streak"
    const val REWARDS = "rewards"
    const val SETTINGS = "settings"

    const val CREATE_TASK = "create_task"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TASK_ID = "taskId"
    const val TASK_DETAIL = "task_detail/{$TASK_ID}"

    const val DATA_MANAGEMENT = "data_management"
    const val RESTORE_DATA = "restore_data"

    fun taskDetail(taskId: Long): String = "task_detail/$taskId"
}
