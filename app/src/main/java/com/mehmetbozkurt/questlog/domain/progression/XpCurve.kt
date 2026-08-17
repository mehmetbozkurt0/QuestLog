package com.mehmetbozkurt.questlog.domain.progression

import kotlin.math.pow
import kotlin.math.roundToInt

object XpCurve {
    const val MIN_STAT = 10
    const val MAX_STAT = 20
    const val MAX_LEVEL = 20
    const val DAILY_STAT_CAP = 150

    private const val STAT_BASE = 100
    private const val STAT_GROWTH = 1.5

    private const val LEVEL_BASE = 300
    private const val LEVEL_GROWTH = 1.45

    val FEAT_LEVELS = setOf(4, 8, 12, 16, 19)

    fun xpForStatIncrease(statValue: Int): Int {
        if (statValue >= MAX_STAT) return Int.MAX_VALUE
        val step = statValue -MIN_STAT
        return (STAT_BASE * STAT_GROWTH.pow(step)).roundToInt()
    }

    fun xpForLevelUp(level: Int): Int {
        if (level >= MAX_LEVEL) return Int.MAX_VALUE
        return (LEVEL_BASE * LEVEL_GROWTH.pow(level-1)).roundToInt()
    }

    fun levelFromTotalXp(totalXp: Int): LevelInfo {
        var level = 1
        var remaining = totalXp

        while (level < MAX_LEVEL) {
            val needed = xpForLevelUp(level)
            if (remaining < needed) break
            remaining -= needed
            level++
        }

        return LevelInfo(
            level = level,
            xpIntoLevel = remaining,
            xpToNextLevel = if (level >= MAX_LEVEL) 0 else xpForLevelUp(level),
        )
    }

    fun featChoicesBetween(oldLevel: Int, newLevel: Int): Int =
        FEAT_LEVELS.count { it in (oldLevel + 1)..newLevel }

    data class LevelInfo(
        val level: Int,
        val xpIntoLevel: Int,
        val xpToNextLevel: Int,
    )
}














