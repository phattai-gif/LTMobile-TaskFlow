package com.team.taskmanagementapp.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.ui.theme.TaskFlowOutline
import com.team.taskmanagementapp.ui.theme.TaskFlowOutlineVariant
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary
import com.team.taskmanagementapp.ui.theme.TaskFlowSurfaceContainerLowest

// Thông tin của một mục trên thanh điều hướng
data class TaskFlowNavItem(
    val id: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
)

@Composable
fun TaskFlowBottomNavigation(
    items: List<TaskFlowNavItem>,
    selectedItemId: String,
    onItemSelected: (String) -> Unit,
    onAddClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val leftItems = items.take(3)
    val rightItems = items.drop(3).take(3)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = TaskFlowSurfaceContainerLowest,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                color = TaskFlowOutlineVariant.copy(alpha = 0.25f),
                thickness = 0.5.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Left 3 items (Home, Calendar, Stats)
                leftItems.forEach { item ->
                    TaskFlowNavItemView(
                        item = item,
                        isSelected = item.id == selectedItemId,
                        onClick = { onItemSelected(item.id) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Center Big Blue (+) Button
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-8).dp)
                            .size(52.dp)
                            .shadow(elevation = 6.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(TaskFlowPrimary)
                            .clickable(onClick = onAddClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.action_create_task),
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Right 3 items (Streak, Badges, Settings)
                rightItems.forEach { item ->
                    TaskFlowNavItemView(
                        item = item,
                        isSelected = item.id == selectedItemId,
                        onClick = { onItemSelected(item.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskFlowNavItemView(
    item: TaskFlowNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) TaskFlowPrimary else TaskFlowOutline

    Column(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(item.iconRes),
            contentDescription = stringResource(item.labelRes),
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )

        Text(
            text = stringResource(item.labelRes),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
