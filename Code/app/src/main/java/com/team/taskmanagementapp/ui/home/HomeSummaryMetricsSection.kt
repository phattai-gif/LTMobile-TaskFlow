package com.team.taskmanagementapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.ui.theme.TaskFlowOutline
import com.team.taskmanagementapp.ui.theme.TaskFlowOutlineVariant
import com.team.taskmanagementapp.ui.theme.TaskFlowOverdue
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary
import com.team.taskmanagementapp.ui.theme.TaskFlowSurfaceContainer
import com.team.taskmanagementapp.ui.theme.TaskFlowSurfaceContainerLowest

@Composable
fun HomeSummaryMetricsSection(
    metrics: HomeSummaryMetrics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: TOTAL TASKS & COMPLETED
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // TOTAL TASKS
            MetricGridCard(
                label = stringResource(R.string.home_total_tasks_label),
                value = metrics.totalCount.toString(),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.home_total_tasks_subtitle),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = TaskFlowOutline
                    )
                )
            }

            // COMPLETED
            val completedProgress = if (metrics.totalCount > 0) {
                metrics.completedCount.toFloat() / metrics.totalCount
            } else 0f

            MetricGridCard(
                label = stringResource(R.string.home_completed_label),
                value = metrics.completedCount.toString(),
                modifier = Modifier.weight(1f)
            ) {
                LinearProgressIndicator(
                    progress = { completedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TaskFlowPrimary,
                    trackColor = TaskFlowSurfaceContainer
                )
            }
        }

        // Row 2: PENDING & OVERDUE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // PENDING
            val pendingProgress = if (metrics.totalCount > 0) {
                metrics.pendingCount.toFloat() / metrics.totalCount
            } else 0f

            MetricGridCard(
                label = stringResource(R.string.home_pending_label),
                value = metrics.pendingCount.toString(),
                modifier = Modifier.weight(1f)
            ) {
                LinearProgressIndicator(
                    progress = { pendingProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TaskFlowPrimary,
                    trackColor = TaskFlowSurfaceContainer
                )
            }

            // OVERDUE
            val isOverdue = metrics.overdueCount > 0
            val overdueColor = if (isOverdue) TaskFlowOverdue else MaterialTheme.colorScheme.onSurface
            val statusColor = if (isOverdue) TaskFlowOverdue else TaskFlowOutline

            MetricGridCard(
                label = stringResource(R.string.home_overdue_label),
                value = metrics.overdueCount.toString(),
                valueColor = overdueColor,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isOverdue) {
                        stringResource(R.string.home_action_needed)
                    } else {
                        stringResource(R.string.home_all_caught_up)
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                )
            }
        }
    }
}

@Composable
private fun MetricGridCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    bottomContent: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TaskFlowSurfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                TaskFlowOutlineVariant.copy(alpha = 0.35f)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = TaskFlowOutline
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = valueColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            bottomContent()
        }
    }
}
