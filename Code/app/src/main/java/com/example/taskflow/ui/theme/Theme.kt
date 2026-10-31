package com.example.taskflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TaskFlowLightColorScheme = lightColorScheme(
    // Màu thương hiệu
    primary = TaskFlowPrimary,
    onPrimary = TaskFlowOnPrimary,
    primaryContainer = TaskFlowPrimaryContainer,
    onPrimaryContainer = TaskFlowOnPrimaryContainer,

    // Màu phụ
    secondary = TaskFlowSecondary,
    onSecondary = TaskFlowOnSecondary,
    secondaryContainer = TaskFlowSecondaryContainer,
    onSecondaryContainer = TaskFlowOnSecondaryContainer,

    // Màu nhấn
    tertiary = TaskFlowTertiary,
    onTertiary = TaskFlowOnTertiary,
    tertiaryContainer = TaskFlowTertiaryContainer,
    onTertiaryContainer = TaskFlowOnTertiaryContainer,

    // Nền và nội dung
    background = TaskFlowBackground,
    onBackground = TaskFlowOnBackground,
    surface = TaskFlowSurface,
    onSurface = TaskFlowOnSurface,
    surfaceVariant = TaskFlowSurfaceVariant,
    onSurfaceVariant = TaskFlowOnSurfaceVariant,

    // Các cấp độ nền cho thành phần Material
    surfaceContainerLowest = TaskFlowSurfaceContainerLowest,
    surfaceContainerLow = TaskFlowSurfaceContainerLow,
    surfaceContainer = TaskFlowSurfaceContainer,
    surfaceContainerHigh = TaskFlowSurfaceContainerHigh,
    surfaceContainerHighest = TaskFlowSurfaceContainerHighest,
    surfaceDim = TaskFlowSurfaceDim,
    surfaceBright = TaskFlowSurface,
    surfaceTint = TaskFlowSurfaceTint,

    // Màu đảo
    inverseSurface = TaskFlowInverseSurface,
    inverseOnSurface = TaskFlowInverseOnSurface,
    inversePrimary = TaskFlowInversePrimary,

    // Đường viền
    outline = TaskFlowOutline,
    outlineVariant = TaskFlowOutlineVariant,

    // Báo lỗi
    error = TaskFlowError,
    onError = TaskFlowOnError,
    errorContainer = TaskFlowErrorContainer,
    onErrorContainer = TaskFlowOnErrorContainer
)

@Composable
fun TaskFLowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TaskFlowLightColorScheme,
        typography = Typography,
        shapes = TaskFlowShapes,
        content = content
    )
}
