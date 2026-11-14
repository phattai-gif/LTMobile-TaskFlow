package com.team.taskmanagementapp.ui.badges

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.data.repository.TaskRepository

@Composable
fun BadgesScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember(context) {
        val database = AppDatabase.getInstance(context)
        TaskRepository(database.taskDao())
    }

    val viewModel = viewModel<BadgesViewModel> {
        BadgesViewModel(repository)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BadgesContent(
        uiState = uiState,
        modifier = modifier
    )
}

@Composable
fun BadgesContent(
    uiState: BadgesUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header Title & Subtitle ─────────────────────────────────────────
        Column(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
            Text(
                text = stringResource(R.string.badges_screen_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.badges_screen_subtitle),
                fontSize = 12.5.sp,
                color = Color(0xFF6B7280),
                lineHeight = 17.sp
            )
        }

        // ── Top Card: BADGES UNLOCKED ───────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.badges_unlocked_label),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${uiState.unlockedCount} / ${uiState.totalBadgesCount}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00C48C)
                )
            }
        }

        // ── Card 1: Streak Milestone Badges ─────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.badges_streak_section_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.streakBadges.forEach { badge ->
                        BadgePillItem(badge = badge)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Footer: Active Streak info
                Text(
                    text = stringResource(R.string.badges_active_streak),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${uiState.currentStreak} Day Streak 🔥",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEA580C)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "best: ${uiState.bestStreak} days",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        R.string.badges_streak_today_progress,
                        uiState.todayCompletedCount,
                        uiState.todayTotalCount
                    ),
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }

        // ── Card 2: Task Completion ─────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.badges_task_section_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.taskBadges.forEach { badge ->
                        BadgePillItem(badge = badge)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Footer: Task Progress info
                Text(
                    text = stringResource(R.string.badges_task_progress_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        R.string.badges_task_completed_format,
                        uiState.totalCompletedTasksCount
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00C48C)
                )
            }
        }

        // ── Card 3: Special Badges ──────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.badges_special_section_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.specialBadges.forEach { badge ->
                        SpecialBadgeCardItem(badge = badge)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
    }
}

@Composable
private fun BadgePillItem(
    badge: BadgeItemUiModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (badge.isUnlocked) Color(0xFFF0FDF4) else Color.White,
        border = BorderStroke(
            1.dp,
            if (badge.isUnlocked) Color(0xFF86EFAC) else Color(0xFFE5E7EB)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${badge.iconEmoji}  ${badge.title}",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (badge.isUnlocked) {
                    stringResource(R.string.badges_status_unlocked)
                } else {
                    stringResource(R.string.badges_status_locked)
                },
                fontSize = 13.sp,
                fontWeight = if (badge.isUnlocked) FontWeight.SemiBold else FontWeight.Normal,
                color = if (badge.isUnlocked) Color(0xFF10B981) else Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
private fun SpecialBadgeCardItem(
    badge: BadgeItemUiModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (badge.isUnlocked) Color(0xFFF0FDF4) else Color.White,
        border = BorderStroke(
            1.dp,
            if (badge.isUnlocked) Color(0xFF86EFAC) else Color(0xFFE5E7EB)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                Text(
                    text = "${badge.iconEmoji}  ${badge.title}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                if (!badge.description.isNullOrEmpty()) {
                    Text(
                        text = badge.description,
                        fontSize = 11.5.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
            Text(
                text = if (badge.isUnlocked) {
                    stringResource(R.string.badges_status_unlocked)
                } else {
                    stringResource(R.string.badges_status_locked)
                },
                fontSize = 13.sp,
                fontWeight = if (badge.isUnlocked) FontWeight.SemiBold else FontWeight.Normal,
                color = if (badge.isUnlocked) Color(0xFF10B981) else Color(0xFF9CA3AF)
            )
        }
    }
}
