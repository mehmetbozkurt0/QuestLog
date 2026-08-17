package com.mehmetbozkurt.questlog.domain.repository

import com.mehmetbozkurt.questlog.domain.model.AcquiredFeat
import com.mehmetbozkurt.questlog.domain.model.CharacterSheet
import com.mehmetbozkurt.questlog.domain.model.FeatId
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.progression.XpResult
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun observeCharacter(): Flow<CharacterSheet?>
    fun observeFeats(): Flow<List<AcquiredFeat>>
    fun observeWeeklyXp(): Flow<Int>

    suspend fun ensureCharacter()
    suspend fun awardXpFor(log: QuestLog): XpAward?
    suspend fun revokeXpFor(logId: String)
    suspend fun chooseFeat(featId: FeatId, chosenStat: StatType?)
}

data class XpAward(
    val result: XpResult,
    val statIncreased: Boolean,
    val newStatValue: Int,
    val leveledUp: Boolean,
    val newLevel: Int,
    val featChoicesGained: Int,
)