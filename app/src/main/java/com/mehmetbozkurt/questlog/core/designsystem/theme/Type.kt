package com.mehmetbozkurt.questlog.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mehmetbozkurt.questlog.R

val CinzelFamily = FontFamily(
    Font(R.font.cinzel_regular, FontWeight.Normal),
    Font(R.font.cinzel_semibold, FontWeight.SemiBold),
    Font(R.font.cinzel_bold, FontWeight.Bold),
)

val GaramondFamily = FontFamily(
    Font(R.font.ebgaramond_regular, FontWeight.Normal),
    Font(R.font.ebgaramond_medium, FontWeight.Medium),
    Font(R.font.ebgaramond_semibold, FontWeight.SemiBold),
    Font(R.font.ebgaramond_italic, FontWeight.Normal, FontStyle.Italic),
)

val QuestLogTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = CinzelFamily, fontWeight = FontWeight.Bold,
        fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = 0.5.sp
    ),
    displayMedium = TextStyle(
        fontFamily = CinzelFamily, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.4.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = CinzelFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.3.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CinzelFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.3.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CinzelFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp, lineHeight = 26.sp, letterSpacing = 0.2.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GaramondFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = GaramondFamily, fontWeight = FontWeight.Normal,
        fontSize = 17.sp, lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = GaramondFamily, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = GaramondFamily, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CinzelFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, letterSpacing = 0.8.sp
    ),
    labelMedium = TextStyle(
        fontFamily = GaramondFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, letterSpacing = 0.5.sp
    ),
)