package com.mehmetbozkurt.questlog.data.repository

import com.mehmetbozkurt.questlog.core.common.IoDispatcher
import com.mehmetbozkurt.questlog.core.database.dao.PathwayDao
import com.mehmetbozkurt.questlog.core.database.entity.PathwayProgressEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayQuestCompletionEntity
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.core.sync.SyncScheduler
import com.mehmetbozkurt.questlog.data.mapper.toDomain
import com.mehmetbozkurt.questlog.data.remote.PathwayRemoteDataSource
import com.mehmetbozkurt.questlog.domain.model.Pathway
import com.mehmetbozkurt.questlog.domain.model.PathwayDetail
import com.mehmetbozkurt.questlog.domain.model.PathwayProgress
import com.mehmetbozkurt.questlog.domain.model.PathwayQuestProgress
import com.mehmetbozkurt.questlog.domain.progression.PathwayRules
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.CompletionOutcome
import com.mehmetbozkurt.questlog.domain.repository.PathwayRepository
import com.mehmetbozkurt.questlog.domain.repository.StartResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PathwayRepositoryImpl @Inject constructor(
    private val dao: PathwayDao,
    private val remote: PathwayRemoteDataSource,
    private val authRepository: AuthRepository,
    private val syncScheduler: SyncScheduler,
    @IoDispatcher private val io: CoroutineDispatcher,
) : PathwayRepository {

    override fun observePathways(): Flow<List<Pathway>> =
        dao.observePathways().map { list -> list.mapNotNull { it.toDomain() } }

    override fun observeProgress(): Flow<List<PathwayProgress>> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else dao.observeProgress(user.uid).map { list -> list.map { it.toDomain() } }
        }

    override fun observeDetail(pathwayId: String): Flow<PathwayDetail?> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) return@flatMapLatest flowOf(null)

            combine(
                dao.observePathways(),
                dao.observeQuestsFor(pathwayId),
                dao.observeProgress(user.uid),
                dao.observeCompletions(user.uid),
            ) { pathways, quests, progressList, completions ->
                val pathway = pathways.firstOrNull { it.id == pathwayId }?.toDomain()
                    ?: return@combine null

                val completionMap = completions.associateBy { it.questId }

                val questProgress = quests.mapNotNull { entity ->
                    entity.toDomain()?.let { quest ->
                        PathwayQuestProgress(
                            quest = quest,
                            completions = completionMap[quest.id]?.completions ?: 0,
                        )
                    }
                }

                PathwayDetail(
                    pathway = pathway,
                    quests = questProgress,
                    progress = progressList
                        .firstOrNull { it.pathwayId == pathwayId }
                        ?.toDomain(),
                )
            }
        }

    override suspend fun refreshCatalog() = withContext(io) {
        runCatching {
            val snapshot = remote.fetchCatalog()
            if (snapshot.pathways.isNotEmpty()) {
                dao.replaceCatalog(snapshot.pathways, snapshot.quests)
            }
        }
        Unit
    }

    override suspend fun startPathway(pathwayId: String): StartResult = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext StartResult.TooManyActive

        val existing = dao.getProgress(user.uid, pathwayId)
        if (existing != null && existing.completedAtMillis == null && existing.abandonedAtMillis == null) {
            return@withContext StartResult.AlreadyStarted
        }

        if (dao.activePathwayCount(user.uid) >= PathwayRules.MAX_ACTIVE_PATHWAYS) {
            return@withContext StartResult.TooManyActive
        }

        val pathway = dao.getPathway(pathwayId) ?: return@withContext StartResult.PrerequisiteMissing

        pathway.requiredPathwayId?.let { requiredId ->
            val required = dao.getProgress(user.uid, requiredId)
            if (required?.completedAtMillis == null) {
                return@withContext StartResult.PrerequisiteMissing
            }
        }

        val now = System.currentTimeMillis()

        if (existing != null) {
            val questIds = dao.getQuestsFor(pathwayId).map { it.id }
            if (questIds.isNotEmpty()) dao.deleteCompletions(user.uid, questIds)
        }

        dao.upsertProgress(
            PathwayProgressEntity(
                userId = user.uid,
                pathwayId = pathwayId,
                startedAtMillis = now,
                lastActivityAtMillis = now,
                escrowedXp = 0,
                completedAtMillis = null,
                abandonedAtMillis = null,
                syncState = SyncState.PENDING.name,
            )
        )

        syncScheduler.requestSync()
        StartResult.Success
    }

    override suspend fun abandonPathway(pathwayId: String) = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext
        val progress = dao.getProgress(user.uid, pathwayId) ?: return@withContext
        if (!progress.isActive()) return@withContext

        dao.upsertProgress(
            progress.copy(
                abandonedAtMillis = System.currentTimeMillis(),
                escrowedXp = 0,
                syncState = SyncState.PENDING.name,
            )
        )

        syncScheduler.requestSync()
    }

    override suspend fun checkInactivePathways() = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext
        val now = Instant.now()
        val limitDays = PathwayRules.INACTIVITY_DAYS.toLong()

        dao.getActiveProgress(user.uid).forEach { progress ->
            val last = Instant.ofEpochMilli(progress.lastActivityAtMillis)
            if (Duration.between(last, now).toDays() >= limitDays) {
                dao.upsertProgress(
                    progress.copy(
                        abandonedAtMillis = now.toEpochMilli(),
                        escrowedXp = 0,
                        syncState = SyncState.PENDING.name,
                    )
                )
            }
        }

        syncScheduler.requestSync()
    }

    override suspend fun recordQuestCompletion(
        questId: String,
        earnedXp: Int,
    ): CompletionOutcome? = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext null
        val quest = dao.getQuest(questId) ?: return@withContext null
        val progress = dao.getProgress(user.uid, quest.pathwayId) ?: return@withContext null
        if (!progress.isActive()) return@withContext null

        val pathway = dao.getPathway(quest.pathwayId) ?: return@withContext null
        val now = System.currentTimeMillis()
        val current = dao.getCompletion(user.uid, questId)
        val newCount = (current?.completions ?: 0) + 1

        dao.upsertCompletion(
            PathwayQuestCompletionEntity(
                userId = user.uid,
                questId = questId,
                completions = newCount,
                lastCompletedAtMillis = now,
                syncState = SyncState.PENDING.name,
            )
        )

        val split = PathwayRules.splitXp(earnedXp)
        val newEscrow = progress.escrowedXp + split.escrowed
        val allQuests = dao.getQuestsFor(quest.pathwayId)
        val completions = dao.observeCompletionsSnapshot(user.uid)
        val completionMap = completions.associateBy { it.questId }

        val isComplete = allQuests.all { q ->
            val done = if (q.id == questId) newCount
            else completionMap[q.id]?.completions ?: 0
            done >= q.requiredCompletions
        }

        val updated = progress.copy(
            lastActivityAtMillis = now,
            escrowedXp = if (isComplete) 0 else newEscrow,
            completedAtMillis = if (isComplete) now else null,
            syncState = SyncState.PENDING.name,
        )
        dao.upsertProgress(updated)

        syncScheduler.requestSync()

        CompletionOutcome(
            pathwayId = quest.pathwayId,
            pathwayTitle = pathway.title,
            escrowedXp = split.escrowed,
            stageUnlocked = null,
            pathwayCompleted = isComplete,
            releasedXp = if (isComplete) newEscrow else 0,
            bonusXp = if (isComplete) pathway.completionBonusXp else 0,
        )
    }

    private fun PathwayProgressEntity.isActive(): Boolean =
        completedAtMillis == null && abandonedAtMillis == null
}