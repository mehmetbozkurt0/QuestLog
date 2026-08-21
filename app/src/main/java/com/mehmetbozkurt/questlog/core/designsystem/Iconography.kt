package com.mehmetbozkurt.questlog.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.ui.graphics.vector.ImageVector
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.StatType

fun StatType.icon(): ImageVector = when (this) {
    StatType.STR -> Icons.Default.FitnessCenter
    StatType.DEX -> Icons.Default.Bolt
    StatType.CON -> Icons.Default.Favorite
    StatType.INT -> Icons.Default.Psychology
    StatType.WIS -> Icons.Default.AutoStories
    StatType.CHA -> Icons.Default.RecordVoiceOver
}

fun Difficulty.pips(): String = when (this) {
    Difficulty.EASY -> "◆"
    Difficulty.MEDIUM -> "◆◆"
    Difficulty.HARD -> "◆◆◆"
    Difficulty.EPIC -> "◆◆◆◆"
}
