package com.staxpayments.sample.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val baseTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 72.sp,
        letterSpacing = 0.5.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 54.sp,
        letterSpacing = 0.5.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.5.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.5.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp,
    )
)

val lightTypography = baseTypography.copy(
    displayLarge = baseTypography.displayLarge.copy(
        color = StaxBlack
    ),
    displayMedium = baseTypography.displayMedium.copy(
        color = StaxBlack
    ),
    displaySmall = baseTypography.displaySmall.copy(
        color = StaxBlack
    ),
    headlineLarge = baseTypography.headlineLarge.copy(
        color = StaxBlack
    ),
    headlineMedium = baseTypography.headlineMedium.copy(
        color = StaxBlack
    ),
    headlineSmall = baseTypography.headlineSmall.copy(
        color = StaxBlack
    ),
    bodyLarge = baseTypography.bodyLarge.copy(
        color = StaxBlack
    ),
    bodyMedium = baseTypography.bodyMedium.copy(
        color = StaxBlack
    ),
    bodySmall = baseTypography.bodySmall.copy(
        color = StaxBlack
    ),
)

val darkTypography = baseTypography.copy(
    displayLarge = baseTypography.displayLarge.copy(
        color = Gray50
    ),
    displayMedium = baseTypography.displayMedium.copy(
        color = Gray50
    ),
    displaySmall = baseTypography.displaySmall.copy(
        color = Gray50
    ),
    headlineLarge = baseTypography.headlineLarge.copy(
        color = Gray50
    ),
    headlineMedium = baseTypography.headlineMedium.copy(
        color = Gray50
    ),
    headlineSmall = baseTypography.headlineSmall.copy(
        color = Gray50
    ),
    bodyLarge = baseTypography.bodyLarge.copy(
        color = Gray50
    ),
    bodyMedium = baseTypography.bodyMedium.copy(
        color = Gray50
    ),
    bodySmall = baseTypography.bodySmall.copy(
        color = Gray50
    ),
)