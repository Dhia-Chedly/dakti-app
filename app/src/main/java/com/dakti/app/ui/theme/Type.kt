package com.dakti.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dakti.app.R

private val DaktiHeadlineFont = FontFamily(
    Font(resId = R.font.teko_variable, weight = FontWeight.Normal),
    Font(resId = R.font.teko_variable, weight = FontWeight.Medium),
    Font(resId = R.font.teko_variable, weight = FontWeight.SemiBold),
    Font(resId = R.font.teko_variable, weight = FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = DaktiHeadlineFont,
        fontWeight = FontWeight.Bold,
        fontSize = 58.sp,
        lineHeight = 60.sp,
        letterSpacing = 0.2.sp
    ),
    displayMedium = TextStyle(
        fontFamily = DaktiHeadlineFont,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.15.sp
    ),
    displaySmall = TextStyle(
        fontFamily = DaktiHeadlineFont,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.15.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DaktiHeadlineFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DaktiHeadlineFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DaktiHeadlineFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DaktiHeadlineFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.1.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 26.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )
)
