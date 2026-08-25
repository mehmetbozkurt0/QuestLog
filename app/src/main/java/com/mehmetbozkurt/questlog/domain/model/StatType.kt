package com.mehmetbozkurt.questlog.domain.model

enum class StatType {
    STR, DEX, CON, INT, WIS, CHA
}

fun StatType.shortName(): String = name
