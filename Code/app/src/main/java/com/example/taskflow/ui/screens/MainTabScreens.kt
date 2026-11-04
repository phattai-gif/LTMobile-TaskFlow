package com.example.taskflow.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.team.taskmanagementapp.R
import com.example.taskflow.ui.theme.TaskFlowDimensions

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    MainTabContent(R.string.nav_home, modifier)
}

@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    MainTabContent(R.string.nav_calendar, modifier)
}

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    MainTabContent(R.string.nav_stats, modifier)
}

@Composable
fun StreakScreen(modifier: Modifier = Modifier) {
    MainTabContent(R.string.nav_streak, modifier)
}

@Composable
fun RewardsScreen(modifier: Modifier = Modifier) {
    MainTabContent(R.string.nav_rewards, modifier)
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    MainTabContent(R.string.nav_settings, modifier)
}

@Composable
private fun MainTabContent(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(TaskFlowDimensions.screenPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}