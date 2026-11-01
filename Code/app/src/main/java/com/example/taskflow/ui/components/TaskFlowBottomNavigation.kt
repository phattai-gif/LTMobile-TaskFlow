package com.example.taskflow.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.taskflow.ui.theme.TaskFlowDimensions

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
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.heightIn(
            min = TaskFlowDimensions.bottomNavigationMinHeight
        ),
        containerColor =
            MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.id == selectedItemId,
                onClick = {
                    onItemSelected(item.id)
                },
                icon = {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(
                            TaskFlowDimensions.iconMedium
                        )
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.labelRes),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor =
                        MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor =
                        MaterialTheme.colorScheme.primary,
                    indicatorColor =
                        MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor =
                        MaterialTheme.colorScheme.outline,
                    unselectedTextColor =
                        MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}