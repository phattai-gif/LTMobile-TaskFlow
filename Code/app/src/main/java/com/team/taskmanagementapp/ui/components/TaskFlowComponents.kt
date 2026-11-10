package com.team.taskmanagementapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.team.taskmanagementapp.ui.theme.TaskFlowDimensions

// Nút chính dùng chung
@Composable
fun TaskFlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(
            min = TaskFlowDimensions.buttonMinHeight
        ),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

// Card dùng chung, nhận nội dung từ màn hình sử dụng
@Composable
fun TaskFlowCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(
                TaskFlowDimensions.cardPadding
            ),
            verticalArrangement = Arrangement.spacedBy(
                TaskFlowDimensions.spacing12
            ),
            content = content
        )
    }
}

// Ô nhập dùng chung, có hỗ trợ hiển thị lỗi
@Composable
fun TaskFlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    errorMessage: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(
            min = TaskFlowDimensions.inputMinHeight
        ),
        enabled = enabled,
        singleLine = singleLine,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = MaterialTheme.shapes.medium,
        isError = errorMessage != null,
        supportingText = if (errorMessage != null) {
            {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            null
        }
    )
}
