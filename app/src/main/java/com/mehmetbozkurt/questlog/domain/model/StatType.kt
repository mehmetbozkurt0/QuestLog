package com.mehmetbozkurt.questlog.domain.model

enum class StatType {
    STR, DEX, CON, INT, WIS, CHA
}

fun StatType.shortName(): String = name

fun StatType.colorHex(): String = when (this) {
    StatType.STR -> "#C1443A"
    StatType.DEX -> "#6E8F6B"
    StatType.CON -> "#8C5A3C"
    StatType.INT -> "#5B8FA8"
    StatType.WIS -> "#7B5EA7"
    StatType.CHA -> "#C8A951"
}
