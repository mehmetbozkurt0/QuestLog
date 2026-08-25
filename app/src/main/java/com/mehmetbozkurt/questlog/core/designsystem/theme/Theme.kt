package com.mehmetbozkurt.questlog.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.mehmetbozkurt.questlog.core.settings.AppPalette

@Immutable
data class QuestLogExtendedColors(
    val priorityLow: Color,
    val priorityMedium: Color,
    val priorityHigh: Color,
    val typeQuest: Color,
    val typeNpc: Color,
    val typeLore: Color,
    val typeSession: Color,
    val statStr: Color,
    val statDex: Color,
    val statCon: Color,
    val statInt: Color,
    val statWis: Color,
    val statCha: Color,
    val celebration: PaletteSpec,
)

private fun darkSchemeOf(s: PaletteSpec): ColorScheme = darkColorScheme(
    primary = s.gold,
    onPrimary = s.bg,
    primaryContainer = s.goldDeep,
    onPrimaryContainer = s.text,
    secondary = s.con,
    onSecondary = s.text,
    tertiary = s.wis,
    onTertiary = s.text,
    background = s.bg,
    onBackground = s.text,
    surface = s.surface,
    onSurface = s.text,
    surfaceVariant = s.surfaceHigh,
    onSurfaceVariant = s.textDim,
    outline = s.outline,
    outlineVariant = s.surfaceHigh,
    error = s.high,
    onError = s.bg,
)

private fun lightSchemeOf(s: PaletteSpec): ColorScheme = lightColorScheme(
    primary = s.gold,
    onPrimary = s.surface,
    primaryContainer = s.surfaceHigh,
    onPrimaryContainer = s.text,
    secondary = s.con,
    onSecondary = s.surface,
    tertiary = s.wis,
    onTertiary = s.surface,
    background = s.bg,
    onBackground = s.text,
    surface = s.surface,
    onSurface = s.text,
    surfaceVariant = s.surfaceHigh,
    onSurfaceVariant = s.textDim,
    outline = s.outline,
    outlineVariant = s.surfaceHigh,
    error = s.high,
    onError = s.surface,
)

private fun extendedOf(s: PaletteSpec, night: PaletteSpec) = QuestLogExtendedColors(
    priorityLow = s.low,
    priorityMedium = s.medium,
    priorityHigh = s.high,
    typeQuest = s.gold,
    typeNpc = s.wis,
    typeLore = s.int,
    typeSession = s.con,
    statStr = s.str,
    statDex = s.dex,
    statCon = s.con,
    statInt = s.int,
    statWis = s.wis,
    statCha = s.cha,
    celebration = night,
)

fun darkSpecOf(palette: AppPalette): PaletteSpec = when (palette) {
    AppPalette.MUREKKEP -> MurekkepDark
    AppPalette.GECE -> GeceDark
    AppPalette.KONTRAST -> KontrastDark
}

private fun lightSpecOf(palette: AppPalette): PaletteSpec = when (palette) {
    AppPalette.MUREKKEP -> MurekkepLight
    AppPalette.GECE -> GeceLight
    AppPalette.KONTRAST -> KontrastLight
}

val LocalExtendedColors = staticCompositionLocalOf {
    extendedOf(darkSpecOf(AppPalette.Default), darkSpecOf(AppPalette.Default))
}

@Composable
fun QuestLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: AppPalette = AppPalette.Default,
    content: @Composable () -> Unit,
) {
    val night = darkSpecOf(palette)
    val spec = if (darkTheme) night else lightSpecOf(palette)
    val colorScheme = if (darkTheme) darkSchemeOf(spec) else lightSchemeOf(spec)

    CompositionLocalProvider(LocalExtendedColors provides extendedOf(spec, night)) {
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
