package com.team.taskmanagementapp.ui.pomodoro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.pomodoro.PomodoroConfig
import com.team.taskmanagementapp.ui.viewmodel.PomodoroViewModel
import kotlin.math.roundToInt

@Composable
internal fun PomodoroSettingsScreen(viewModel: PomodoroViewModel, onBack: () -> Unit) {
    val config by viewModel.config.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { PomodoroTopBar(stringResource(R.string.pomodoro_settings_title), onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.pomodoro_settings_durations_section),
                style = MaterialTheme.typography.titleMedium
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    DurationSlider(
                        title = stringResource(R.string.pomodoro_session_focus),
                        minutes = config.focusMinutes,
                        range = PomodoroConfig.FOCUS_MINUTES_RANGE,
                        step = 5,
                        onChange = { minutes -> viewModel.updateConfig { it.copy(focusMinutes = minutes) } }
                    )
                    DurationSlider(
                        title = stringResource(R.string.pomodoro_session_short_break),
                        minutes = config.shortBreakMinutes,
                        range = PomodoroConfig.SHORT_BREAK_MINUTES_RANGE,
                        step = 1,
                        onChange = { minutes -> viewModel.updateConfig { it.copy(shortBreakMinutes = minutes) } }
                    )
                    DurationSlider(
                        title = stringResource(R.string.pomodoro_session_long_break),
                        minutes = config.longBreakMinutes,
                        range = PomodoroConfig.LONG_BREAK_MINUTES_RANGE,
                        step = 5,
                        onChange = { minutes -> viewModel.updateConfig { it.copy(longBreakMinutes = minutes) } }
                    )
                }
            }
            Text(
                stringResource(R.string.pomodoro_settings_auto_section),
                style = MaterialTheme.typography.titleMedium
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                SettingSwitch(
                    title = stringResource(R.string.pomodoro_settings_auto_start_breaks),
                    description = stringResource(R.string.pomodoro_settings_auto_start_breaks_desc),
                    checked = config.autoStartBreaks,
                    onChange = { enabled -> viewModel.updateConfig { it.copy(autoStartBreaks = enabled) } }
                )
                SettingSwitch(
                    title = stringResource(R.string.pomodoro_settings_auto_start_focus),
                    description = stringResource(R.string.pomodoro_settings_auto_start_focus_desc),
                    checked = config.autoStartFocus,
                    onChange = { enabled -> viewModel.updateConfig { it.copy(autoStartFocus = enabled) } }
                )
            }
            Text(
                stringResource(R.string.pomodoro_settings_applied_note),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DurationSlider(
    title: String,
    minutes: Int,
    range: IntRange,
    step: Int,
    onChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(
            stringResource(R.string.pomodoro_settings_minutes_format, minutes),
            color = MaterialTheme.colorScheme.primary
        )
    }
    Slider(
        value = minutes.toFloat(),
        onValueChange = { value ->
            val next = value.roundToInt()
            if (next != minutes) onChange(next)
        },
        valueRange = range.first.toFloat()..range.last.toFloat(),
        steps = (range.last - range.first) / step - 1
    )
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}
