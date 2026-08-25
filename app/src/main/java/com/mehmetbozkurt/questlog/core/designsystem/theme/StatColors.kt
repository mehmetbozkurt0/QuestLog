package com.mehmetbozkurt.questlog.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mehmetbozkurt.questlog.domain.model.StatType

fun PaletteSpec.statColor(type: StatType): Color = when (type) {
    StatType.STR -> str
    StatType.DEX -> dex
    StatType.CON -> con
    StatType.INT -> int
    StatType.WIS -> wis
    StatType.CHA -> cha
}

@Composable
fun StatType.color(): Color {
    val colors = MaterialTheme.extendedColors
    return when (this) {
        StatType.STR -> colors.statStr
        StatType.DEX -> colors.statDex
        StatType.CON -> colors.statCon
        StatType.INT -> colors.statInt
        StatType.WIS -> colors.statWis
        StatType.CHA -> colors.statCha
    }
}
