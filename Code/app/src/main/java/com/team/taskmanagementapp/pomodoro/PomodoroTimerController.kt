package com.team.taskmanagementapp.pomodoro

internal object PomodoroTimerController {
    // Service commands and settings changes both run on the main thread.
    val engine = PomodoroTimerEngine()
    val state = engine.snapshot
    val snapshot: PomodoroSnapshot
        get() = engine.currentSnapshot
}
