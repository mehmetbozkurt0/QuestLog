package com.mehmetbozkurt.questlog.domain.repository

import com.mehmetbozkurt.questlog.domain.model.AcquiredFeat
import com.mehmetbozkurt.questlog.domain.model.CharacterSheet
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.FeatId
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.model.WeeklySummary
import com.mehmetbozkurt.questlog.domain.progression.StreakInfo
import com.mehmetbozkurt.questlog.domain.progression.XpResult
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun observeCharacter(): Flow<CharacterSheet?>
    fun observeFeats(): Flow<List<AcquiredFeat>>
    fun observeStreak(): Flow<StreakInfo>
    fun observeWeeklySummary(): Flow<WeeklySummary>

    suspend fun ensureCharacter()
    suspend fun awardXpFor(log: QuestLog): XpAward?
    suspend fun revokeXpFor(logId: String)
    suspend fun chooseFeat(featId: FeatId, chosenStat: StatType?)

    suspend fun awardSplitXp(
        statType: StatType,
        difficulty: Difficulty,
        logId: String,
        immediateCharacterXp: Int,
        fullStatXp: Int,
    ): XpAward.Granted?

    suspend fun awardCharacterOnlyXp(amount: Int): LevelUpInfo
}

sealed interface XpAward {
    data class Granted(
        val result: XpResult,
        val statType: StatType,
        val statIncreased: Boolean,
        val newStatValue: Int,
        val leveledUp: Boolean,
        val newLevel: Int,
        val featChoicesGained: Int,
        val streakMilestone: Int? = null
    ) : XpAward

    data class Rejected(val reason: RejectReason) : XpAward

    enum class RejectReason {
        ALREADY_AWARDED_TODAY,
        DAILY_DIFFICULTY_LIMIT,
        DAILY_STAT_CAP,
        NOT_ELIGIBLE,
    }
}

data class LevelUpInfo(
    val leveledUp: Boolean,
    val newLevel: Int,
    val featChoicesGained: Int,
)