package com.mehmetbozkurt.questlog.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class QuestLogExtendedColors(
    val priorityLow: Color,
    val priorityMedium: Color,
    val priorityHigh: Color,
    val typeQuest: Color,
    val typeNpc: Color,
    val typeLore: Color,
    val typeSession: Color,
)

private val DarkExtended = QuestLogExtendedColors(
    priorityLow = PriorityLowDark,
    priorityMedium = PriorityMediumDark,
    priorityHigh = PriorityHighDark,
    typeQuest = TypeQuestDark,
    typeNpc = TypeNpcDark,
    typeLore = TypeLoreDark,
    typeSession = TypeSessionDark,
)

private val LightExtended = QuestLogExtendedColors(
    priorityLow = PriorityLowLight,
    priorityMedium = PriorityMediumLight,
    priorityHigh = PriorityHighLight,
    typeQuest = TypeQuestLight,
    typeNpc = TypeNpcLight,
    typeLore = TypeLoreLight,
    typeSession = TypeSessionLight,
)

val LocalExtendedColors = staticCompositionLocalOf { DarkExtended }

private val DarkScheme = darkColorScheme(
    primary = AgedGold,
    onPrimary = InkBlack,
    primaryContainer = AgedGoldDim,
    onPrimaryContainer = Parchment,
    secondary = Leather,
    onSecondary = Parchment,
    tertiary = ArcanePurple,
    onTertiary = Parchment,
    background = InkBlack,
    onBackground = Parchment,
    surface = InkSurface,
    onSurface = Parchment,
    surfaceVariant = InkSurfaceHigh,
    onSurfaceVariant = ParchmentDim,
    outline = InkOutline,
    outlineVariant = InkSurfaceHigh,
    error = BloodRed,
    onError = InkBlack,
)

private val LightScheme = lightColorScheme(
    primary = AgedGoldDim,
    onPrimary = PaperSurface,
    primaryContainer = PaperSurfaceAlt,
    onPrimaryContainer = DarkInk,
    secondary = Leather,
    onSecondary = PaperSurface,
    tertiary = ArcanePurple,
    onTertiary = PaperSurface,
    background = PaperBase,
    onBackground = DarkInk,
    surface = PaperSurface,
    onSurface = DarkInk,
    surfaceVariant = PaperSurfaceAlt,
    onSurfaceVariant = DarkInkDim,
    outline = PaperOutline,
    outlineVariant = PaperSurfaceAlt,
    error = CrimsonInk,
    onError = PaperSurface,
)

@Composable
fun QuestLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val extended = if (darkTheme) DarkExtended else LightExtended

    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = QuestLogTypography,
            shapes = QuestLogShapes,
            content = content
        )
    }
}

val MaterialTheme.extendedColors: QuestLogExtendedColors
    @Composable get() = LocalExtendedColors.current