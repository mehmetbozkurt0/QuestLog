package com.mehmetbozkurt.questlog.domain.model

data class StatProgress(
    val statType: StatType,
    val value: Int,
    val currentXp: Int,
    val xpToNext: Int,
)

data class CharacterSheet(
    val userId: String,
    val level: Int,
    val totalXp: Int,
    val xpIntoLevel: Int,
    val xpToNextLevel: Int,
    val pendingFeatChoices: Int,
    val stats: List<StatProgress>,
    val createdAt: java.time.Instant,
    val epicBoons: Int = 0
) {
    fun stat(type: StatType): StatProgress =
        stats.first { it.statType == type }
}