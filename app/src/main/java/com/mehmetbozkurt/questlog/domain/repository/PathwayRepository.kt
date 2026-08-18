package com.mehmetbozkurt.questlog.domain.repository

import com.mehmetbozkurt.questlog.domain.model.Pathway
import com.mehmetbozkurt.questlog.domain.model.PathwayDetail
import com.mehmetbozkurt.questlog.domain.model.PathwayProgress
import com.mehmetbozkurt.questlog.domain.model.StatType
import kotlinx.coroutines.flow.Flow

interface PathwayRepository {
    fun observePathways(): Flow<List<Pathway>>
    fun observeProgress(): Flow<List<PathwayProgress>>
    fun observeDetail(pathwayId: String): Flow<PathwayDetail?>

    suspend fun refreshCatalog()
    suspend fun startPathway(pathwayId: String): StartResult
    suspend fun abandonPathway(pathwayId: String)
    suspend fun checkInactivePathways()

    suspend fun completeQuest(questId: String): QuestCompletionResult
}

sealed interface StartResult {
    data object Success : StartResult
    data object TooManyActive : StartResult
    data object PrerequisiteMissing : StartResult
    data object AlreadyStarted : StartResult
}

data class CompletionOutcome(
    val pathwayId: String,
    val pathwayTitle: String,
    val escrowedXp: Int,
    val stageUnlocked: Int?,
    val pathwayCompleted: Boolean,
    val releasedXp: Int,
    val bonusXp: Int,
)

sealed interface QuestCompletionResult {
    data class Success(
        val questTitle: String,
        val statType: StatType,
        val immediateXp: Int,
        val escrowedXp: Int,
        val statIncreased: Boolean,
        val newStatValue: Int,
        val leveledUp: Boolean,
        val newLevel: Int,
        val featChoicesGained: Int,
        val stageCompleted: Boolean,
        val pathwayCompleted: Boolean,
        val releasedXp: Int,
        val bonusXp: Int,
    ) : QuestCompletionResult

    data class Rejected(val reason: String) : QuestCompletionResult
}