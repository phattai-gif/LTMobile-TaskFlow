package com.example.taskflow.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val TaskFlowShapes = Shapes(
    extraSmall = RoundedCornerShape(
        TaskFlowDimensions.cornerExtraSmall
    ),
    small = RoundedCornerShape(
        TaskFlowDimensions.cornerSmall
    ),
    medium = RoundedCornerShape(
        TaskFlowDimensions.cornerMedium
    ),
    large = RoundedCornerShape(
        TaskFlowDimensions.cornerLarge
    ),
    extraLarge = RoundedCornerShape(
        TaskFlowDimensions.cornerExtraLarge
    )
)