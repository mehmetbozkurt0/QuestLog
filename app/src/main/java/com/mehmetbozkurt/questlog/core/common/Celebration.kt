package com.mehmetbozkurt.questlog.core.common

import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.repository.QuestCompletionResult
import com.mehmetbozkurt.questlog.domain.repository.XpAward

enum class CelebrationTier {MINOR, MAJOR, EPIC}

data class Celebration(
    val xpGained: Int,
    val statType: StatType,
    val escrowedXp: Int = 0,
    val statIncreased: Boolean = false,
    val newStatValue: Int = 0,
    val leveledUp: Boolean = false,
    val newLevel: Int = 0,
    val featChoicesGained: Int = 0,
    val stageCompleted: Boolean = false,
    val pathwayCompleted: Boolean = false,
    val completionBonusXp: Int = 0,
    val bonuses: List<String> = emptyList(),
    val streakMilestone: Int? = null
) {
    val tier: CelebrationTier
        get() = when {
            leveledUp || pathwayCompleted -> CelebrationTier.EPIC
            statIncreased || stageCompleted || featChoicesGained > 0 || streakMilestone != null -> CelebrationTier.MAJOR
            else -> CelebrationTier.MINOR
        }
}

fun XpAward.Granted.toCelebration() = Celebration(
    xpGained = result.finalXp,
    statType = statType,
    statIncreased = statIncreased,
    newStatValue = newStatValue,
    leveledUp = leveledUp,
    newLevel = newLevel,
    featChoicesGained = featChoicesGained,
    bonuses = result.appliedBonuses,
    streakMilestone = streakMilestone
)

fun QuestCompletionResult.Success.toCelebration() = Celebration(
    xpGained = immediateXp,
    statType = statType,
    escrowedXp = escrowedXp,
    statIncreased = statIncreased,
    newStatValue = newStatValue,
    leveledUp = leveledUp,
    newLevel = newLevel,
    featChoicesGained = featChoicesGained,
    stageCompleted = stageCompleted,
    pathwayCompleted = pathwayCompleted,
    completionBonusXp = releasedXp + bonusXp,
    streakMilestone = streakMilestone
)