package com.mehmetbozkurt.questlog.domain.model

enum class Difficulty(val baseXp: Int) {
    EASY(10),
    MEDIUM(25),
    HARD(60),
    EPIC(150),
}
