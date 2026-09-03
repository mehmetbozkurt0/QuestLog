package com.mehmetbozkurt.questlog.domain.repository

import com.mehmetbozkurt.questlog.domain.model.CatalogEntry
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    fun observeCatalog(): Flow<List<CatalogEntry>>
    suspend fun refreshCatalog(): Boolean
    suspend fun completeTask(taskId: String): CatalogCompletionResult
}

sealed interface CatalogCompletionResult {
    data class Success(val award: XpAward.Granted) : CatalogCompletionResult
    data class Rejected(val reason: CatalogRejection) : CatalogCompletionResult
}

enum class CatalogRejection {
    NO_SESSION,
    TASK_NOT_FOUND,
    ALREADY_DONE_TODAY,
    DAILY_LIMIT,
    XP_NOT_AWARDED,
}
