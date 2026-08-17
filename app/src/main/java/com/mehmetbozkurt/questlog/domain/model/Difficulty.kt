package com.mehmetbozkurt.questlog.domain.model

enum class Difficulty(val baseXp: Int) {
    EASY(10),
    MEDIUM(25),
    HARD(60),
    EPIC(150),
}

fun Difficulty.displayName(): String = when (this) {
    Difficulty.EASY -> "Kolay"
    Difficulty.MEDIUM -> "Orta"
    Difficulty.HARD -> "Zor"
    Difficulty.EPIC -> "Destansı"
}

fun Difficulty.hint(): String = when (this) {
    Difficulty.EASY -> "5-10 dakika"
    Difficulty.MEDIUM -> "30-60 dakika"
    Difficulty.HARD -> "2+ saat"
    Difficulty.EPIC -> "Bir günlük iş"
}