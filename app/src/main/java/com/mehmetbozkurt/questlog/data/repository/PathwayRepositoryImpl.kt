package com.mehmetbozkurt.questlog.data.repository

import com.mehmetbozkurt.questlog.core.common.IoDispatcher
import com.mehmetbozkurt.questlog.core.common.startOfTodayMillis
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
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.PathwayRepository
import com.mehmetbozkurt.questlog.domain.repository.QuestCompletionResult
import com.mehmetbozkurt.questlog.domain.repository.QuestRejection
import com.mehmetbozkurt.questlog.domain.repository.StartResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val characterRepository: CharacterRepository,
    @IoDispatcher private val io: CoroutineDispatcher,
) : PathwayRepository {
    private val completionMutex = Mutex()

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

    override suspend fun detailSnapshot(pathwayId: String): PathwayDetail? =
        withContext(io) {
            val user = authRepository.currentUserSync() ?: return@withContext null
            val pathway = dao.getPathway(pathwayId)?.toDomain() ?: return@withContext null
            val questEntities = dao.getQuestsFor(pathwayId)
            val completionMap = dao.getCompletionsSnapshot(user.uid).associateBy { it.questId }

            val questProgress = questEntities.mapNotNull { entity ->
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
                progress = dao.getProgress(user.uid, pathwayId)?.toDomain(),
            )
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

    override suspend fun completeQuest(questId: String): QuestCompletionResult =
        withContext(io){
            completionMutex.withLock{
                val user = authRepository.currentUserSync()
                    ?: return@withLock QuestCompletionResult.Rejected(QuestRejection.NO_SESSION)

                val quest = dao.getQuest(questId)?.toDomain()
                    ?: return@withLock QuestCompletionResult.Rejected(QuestRejection.QUEST_NOT_FOUND)

                val progress = dao.getProgress(user.uid, quest.pathwayId)
                    ?: return@withLock QuestCompletionResult.Rejected(QuestRejection.NOT_ENROLLED)

                if (!progress.isActive()) {
                    return@withLock QuestCompletionResult.Rejected(QuestRejection.PATHWAY_INACTIVE)
                }

                val pathwayEntity = dao.getPathway(quest.pathwayId)
                    ?: return@withLock QuestCompletionResult.Rejected(QuestRejection.PATHWAY_NOT_FOUND)

                val allQuestEntities = dao.getQuestsFor(quest.pathwayId)
                val completionMap = dao.getCompletionsSnapshot(user.uid).associateBy { it.questId }

                val unlockedStage = allQuestEntities
                    .groupBy { it.stage }
                    .toSortedMap()
                    .entries
                    .firstOrNull { (_, quests) ->
                        quests.any {
                            (completionMap[it.id]?.completions ?: 0) < it.requiredCompletions
                        }
                    }?.key ?: quest.stage

                if (quest.stage > unlockedStage) {
                    return@withLock QuestCompletionResult.Rejected(QuestRejection.STAGE_LOCKED)
                }

                val currentCount = completionMap[questId]?.completions ?: 0
                if (currentCount >= quest.requiredCompletions) {
                    return@withLock QuestCompletionResult.Rejected(QuestRejection.ALREADY_COMPLETED)
                }

                val todayStart = startOfTodayMillis()
                val lastCompleted = completionMap[questId]?.lastCompletedAtMillis ?: 0L
                if (lastCompleted >= todayStart) {
                    return@withLock QuestCompletionResult.Rejected(QuestRejection.ALREADY_DONE_TODAY)
                }

                val now = System.currentTimeMillis()
                val baseXp = quest.difficulty.baseXp
                val split = PathwayRules.splitXp(baseXp)

                val award = characterRepository.awardSplitXp(
                    statType = quest.statType,
                    difficulty = quest.difficulty,
                    logId = questId,
                    title = quest.title,
                    immediateCharacterXp = split.immediate,
                    fullStatXp = baseXp,
                ) ?: return@withLock QuestCompletionResult.Rejected(QuestRejection.XP_NOT_AWARDED)

                val newCount = currentCount + 1
                dao.upsertCompletion(
                    PathwayQuestCompletionEntity(
                        userId = user.uid,
                        questId = questId,
                        completions = newCount,
                        lastCompletedAtMillis = now,
                        syncState = SyncState.PENDING.name,
                    )
                )

                fun completionsFor(id: String): Int =
                    if (id == questId) newCount else completionMap[id]?.completions ?: 0

                val stageQuests = allQuestEntities.filter { it.stage == quest.stage }
                val stageDone = stageQuests.all {
                    completionsFor(it.id) >= it.requiredCompletions
                }
                val pathwayDone = allQuestEntities.all {
                    completionsFor(it.id) >= it.requiredCompletions
                }

                val newEscrow = progress.escrowedXp + split.escrowed
                var releasedXp = 0
                var bonusXp = 0
                var leveledUp = award.leveledUp
                var newLevel = award.newLevel
                var featGained = award.featChoicesGained

                if (pathwayDone) {
                    releasedXp = newEscrow
                    bonusXp = pathwayEntity.completionBonusXp
                    val releaseInfo = characterRepository.awardCharacterOnlyXp(releasedXp + bonusXp)
                    leveledUp = leveledUp || releaseInfo.leveledUp
                    newLevel = releaseInfo.newLevel
                    featGained += releaseInfo.featChoicesGained
                }

                dao.upsertProgress(
                    progress.copy(
                        lastActivityAtMillis = now,
                        escrowedXp = if (pathwayDone) 0 else newEscrow,
                        completedAtMillis = if (pathwayDone) now else null,
                        syncState = SyncState.PENDING.name,
                    )
                )

                syncScheduler.requestSync()

                QuestCompletionResult.Success(
                    questTitle = quest.title,
                    statType = quest.statType,
                    immediateXp = split.immediate,
                    escrowedXp = split.escrowed,
                    statIncreased = award.statIncreased,
                    newStatValue = award.newStatValue,
                    leveledUp = leveledUp,
                    newLevel = newLevel,
                    featChoicesGained = featGained,
                    stageCompleted = stageDone && !pathwayDone,
                    pathwayCompleted = pathwayDone,
                    releasedXp = releasedXp,
                    bonusXp = bonusXp,
                    streakMilestone = award.streakMilestone
                )
            }
        }

    private fun PathwayProgressEntity.isActive(): Boolean =
        completedAtMillis == null && abandonedAtMillis == null
}