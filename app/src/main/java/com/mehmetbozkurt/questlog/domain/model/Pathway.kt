package com.mehmetbozkurt.questlog.domain.model

import java.time.Instant

data class Pathway(
    val id: String,
    val title: String,
    val description: String,
    val primaryStat: StatType,
    val secondaryStat: StatType?,
    val tier: Int,
    val requiredPathwayId: String?,
    val completionBonusXp: Int,
    val sortOrder: Int
)

data class PathwayQuest(
    val id: String,
    val pathwayId: String,
    val title: String,
    val description: String,
    val statType: StatType,
    val difficulty: Difficulty,
    val stage: Int,
    val requiredCompletions: Int,
    val sortOrder: Int,
)

data class PathwayProgress(
    val pathwayId: String,
    val userId: String,
    val startedAt: Instant,
    val lastActivityAt: Instant,
    val escrowedXp: Int,
    val completedAt: Instant?,
    val abandonedAt: Instant?,
) {
    val isActive: Boolean get() = completedAt == null && abandonedAt == null
    val isCompleted: Boolean get() = completedAt != null
}

data class PathwayDetail(
    val pathway: Pathway,
    val quests: List<PathwayQuestProgress>,
    val progress: PathwayProgress?,
) {
    val stages: Map<Int, List<PathwayQuestProgress>>
        get() = quests.groupBy { it.quest.stage }.toSortedMap()

    val unlockedStage: Int
        get() {
            val sorted = stages.keys.sorted()
            for (stage in sorted) {
                val questsInStage = stages[stage].orEmpty()
                if (questsInStage.any { !it.isComplete }) return stage
            }
            return sorted.lastOrNull() ?: 1
        }

    fun isStageUnlocked(stage: Int): Boolean = stage <= unlockedStage

    val totalQuests: Int get() = quests.size
    val completedQuests: Int get() = quests.count { it.isComplete }

    val isFullyComplete: Boolean
        get() = quests.isNotEmpty() && quests.all { it.isComplete }

    val progressFraction: Float
        get() = if (totalQuests == 0) 0f else completedQuests.toFloat() / totalQuests
}

data class PathwayQuestProgress(
    val quest: PathwayQuest,
    val completions: Int,
) {
    val isComplete: Boolean get() = completions >= quest.requiredCompletions
    val fraction: Float
        get() = (completions.toFloat() / quest.requiredCompletions).coerceIn(0f, 1f)
}