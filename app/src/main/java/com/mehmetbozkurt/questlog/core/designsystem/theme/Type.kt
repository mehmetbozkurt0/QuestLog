package com.mehmetbozkurt.questlog.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mehmetbozkurt.questlog.R

val CinzelFamily = FontFamily(
    Font(R.font.cinzel_variable, FontWeight.Normal),
    Font(R.font.cinzel_variable, FontWeight.Medium),
    Font(R.font.cinzel_variable, FontWeight.SemiBold),
    Font(R.font.cinzel_variable, FontWeight.Bold),
    Font(R.font.cinzel_variable, FontWeight.Black),
)

val GaramondFamily = FontFamily(
    Font(R.font.eb_garamond_variable, FontWeight.Normal),
    Font(R.font.eb_garamond_variable, FontWeight.Medium),
    Font(R.font.eb_garamond_variable, FontWeight.SemiBold),
    Font(R.font.eb_garamond_variable, FontWeight.Bold),
)

private fun cinzel(size: Int, weight: FontWeight, lineHeight: Int, letterSpacing: Float) =
    TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing.sp,
    )

private fun garamond(size: Int, weight: FontWeight, lineHeight: Int, letterSpacing: Float = 0f) =
    TextStyle(
        fontFamily = GaramondFamily,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing.sp,
    )

val QuestLogTypography = Typography(
    displayLarge = cinzel(38, FontWeight.Black, 46, 1.0f),
    displayMedium = cinzel(28, FontWeight.Bold, 36, 1.0f),
    displaySmall = cinzel(23, FontWeight.Bold, 30, 1.2f),

    headlineLarge = cinzel(19, FontWeight.Bold, 26, 1.4f),
    headlineMedium = cinzel(17, FontWeight.SemiBold, 24, 1.2f),
    headlineSmall = cinzel(15, FontWeight.SemiBold, 22, 1.0f),

    titleLarge = garamond(20, FontWeight.SemiBold, 27),
    titleMedium = garamond(18, FontWeight.SemiBold, 24),
    titleSmall = garamond(16, FontWeight.SemiBold, 22),

    bodyLarge = garamond(18, FontWeight.Normal, 27),
    bodyMedium = garamond(17, FontWeight.Normal, 25),
    bodySmall = garamond(15, FontWeight.Normal, 21),

    labelLarge = garamond(17, FontWeight.SemiBold, 22),
    labelMedium = cinzel(12, FontWeight.Bold, 16, 0.6f),
    labelSmall = cinzel(11, FontWeight.SemiBold, 15, 1.0f),
)

val ContentHero = garamond(26, FontWeight.Bold, 34)
