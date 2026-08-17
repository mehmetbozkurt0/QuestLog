package com.mehmetbozkurt.questlog.domain.progression

import com.mehmetbozkurt.questlog.domain.model.Difficulty

object XpLimits {
    const val ONE_AWARD_PER_LOG_PER_DAY = true

    fun dailyLimitFor(difficulty: Difficulty): Int? = when (difficulty) {
        Difficulty.EASY -> null
        Difficulty.MEDIUM -> null
        Difficulty.HARD -> 3
        Difficulty.EPIC -> 1
    }
}















