package com.team.taskmanagementapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.ui.theme.TaskFlowOutline
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary

@Composable
fun HomeStreakSection(
    streakInfo: HomeStreakInfo,
    modifier: Modifier = Modifier
) {
    val warmBorderColor = Color(0xFFFDE68A)
    val warmBgColor = Color(0xFFFFFDF8)
    val flameColor = Color(0xFFF97316)
    val bestBadgeBg = Color(0xFFFEF3C7)
    val bestBadgeText = Color(0xFFD97706)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = warmBgColor
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(warmBorderColor)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Flame icon
                    Text(
                        text = "🔥",
                        fontSize = 28.sp
                    )

                    Column {
                        Text(
                            text = stringResource(R.string.home_streak_title, streakInfo.currentStreak),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = stringResource(R.string.home_streak_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp
                            ),
                            color = TaskFlowOutline
                        )
                    }
                }

                // Best streak pill badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bestBadgeBg)
                        .border(1.dp, warmBorderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_streak_best, streakInfo.bestStreak),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = bestBadgeText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row: 7 Days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                streakInfo.weekDays.forEach { day ->
                    DayCircleItem(day = day)
                }
            }
        }
    }
}

@Composable
private fun DayCircleItem(
    day: StreakDayUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Day Letter (T, T, T, T, T, T, C)
        Text(
            text = day.dayLetter,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = if (day.isToday) TaskFlowPrimary else TaskFlowOutline
        )

        // Day Number Circle
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (day.isToday) Color(0xFFEFF6FF) else Color(0xFFF3F4F6)
                )
                .border(
                    width = if (day.isToday) 2.dp else 0.dp,
                    color = if (day.isToday) TaskFlowPrimary else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.dayNumber.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                ),
                color = if (day.isToday) TaskFlowPrimary else Color(0xFF6B7280)
            )
        }
    }
}
