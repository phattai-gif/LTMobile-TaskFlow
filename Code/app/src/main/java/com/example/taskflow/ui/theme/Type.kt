package com.example.taskflow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.team.taskmanagementapp.R
// Một họ font với bốn mức độ đậm
val TaskFlowFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

// Hàm tạo kiểu chữ, giúp tránh lặp lại cấu hình
private fun taskFlowTextStyle(
    size: Int,
    height: Int,
    weight: FontWeight
): TextStyle {
    return TextStyle(
        fontFamily = TaskFlowFontFamily,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = height.sp,
        letterSpacing = 0.sp
    )
}

// Bộ kiểu chữ dùng chung cho toàn ứng dụng
val Typography = Typography(
    // Tiêu đề nổi bật
    displayLarge = taskFlowTextStyle(40, 48, FontWeight.Bold),
    displayMedium = taskFlowTextStyle(32, 40, FontWeight.SemiBold),
    displaySmall = taskFlowTextStyle(28, 36, FontWeight.SemiBold),

    // Tiêu đề màn hình hoặc khu vực
    headlineLarge = taskFlowTextStyle(24, 32, FontWeight.SemiBold),
    headlineMedium = taskFlowTextStyle(20, 28, FontWeight.Medium),
    headlineSmall = taskFlowTextStyle(18, 26, FontWeight.Medium),

    // Tiêu đề thành phần
    titleLarge = taskFlowTextStyle(20, 28, FontWeight.SemiBold),
    titleMedium = taskFlowTextStyle(16, 24, FontWeight.Medium),
    titleSmall = taskFlowTextStyle(14, 20, FontWeight.Medium),

    // Nội dung
    bodyLarge = taskFlowTextStyle(16, 24, FontWeight.Normal),
    bodyMedium = taskFlowTextStyle(14, 20, FontWeight.Normal),
    bodySmall = taskFlowTextStyle(12, 18, FontWeight.Normal),

    // Nhãn nút và nhãn thông tin
    labelLarge = taskFlowTextStyle(14, 20, FontWeight.Medium),
    labelMedium = taskFlowTextStyle(12, 16, FontWeight.Medium),
    labelSmall = taskFlowTextStyle(10, 14, FontWeight.Medium)
)