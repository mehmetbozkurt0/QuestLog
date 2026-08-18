package com.mehmetbozkurt.questlog.data.repository

import com.mehmetbozkurt.questlog.core.common.IoDispatcher
import com.mehmetbozkurt.questlog.core.database.dao.CatalogDao
import com.mehmetbozkurt.questlog.data.mapper.toDomain
import com.mehmetbozkurt.questlog.domain.model.CatalogQuest
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.ProofLevel
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.CatalogRepository
import com.mehmetbozkurt.questlog.domain.repository.QuestLogRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val dao: CatalogDao,
    private val questLogRepository: QuestLogRepository,
    private val authRepository: AuthRepository,
    @IoDispatcher private val io: CoroutineDispatcher,
) : CatalogRepository {

    override fun observeCatalog(): Flow<List<CatalogQuest>> =
        dao.observeAll().map { list -> list.mapNotNull { it.toDomain() } }

    override suspend fun addToMyQuests(catalogQuest: CatalogQuest) = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext
        val now = Instant.now()

        questLogRepository.upsert(
            QuestLog(
                id = questLogRepository.newId(),
                ownerId = user.uid,
                campaignId = null,
                type = LogType.QUEST,
                title = catalogQuest.title,
                description = catalogQuest.description,
                categoryId = null,
                priority = Priority.MEDIUM,
                dueAt = null,
                remindAt = null,
                isCompleted = false,
                createdAt = now,
                updatedAt = now,
                statType = catalogQuest.statType,
                difficulty = catalogQuest.difficulty,
                proofLevel = ProofLevel.NONE,
                proofNote = null,
                completedAt = null,
                pathwayQuestId = null
            )
        )
    }
}