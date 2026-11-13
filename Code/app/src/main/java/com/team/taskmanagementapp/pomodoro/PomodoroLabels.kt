package com.team.taskmanagementapp.pomodoro

import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.data.model.enums.SessionType

internal val SessionType.pomodoroLabel: Int
    get() = when (this) {
        SessionType.FOCUS -> R.string.pomodoro_session_focus
        SessionType.SHORT_BREAK -> R.string.pomodoro_session_short_break
        SessionType.LONG_BREAK -> R.string.pomodoro_session_long_break
    }

internal val PomodoroTimerState.pomodoroLabel: Int
    get() = when (this) {
        PomodoroTimerState.IDLE -> R.string.pomodoro_status_idle
        PomodoroTimerState.RUNNING -> R.string.pomodoro_status_running
        PomodoroTimerState.PAUSED -> R.string.pomodoro_status_paused
        PomodoroTimerState.COMPLETED -> R.string.pomodoro_status_completed
    }
