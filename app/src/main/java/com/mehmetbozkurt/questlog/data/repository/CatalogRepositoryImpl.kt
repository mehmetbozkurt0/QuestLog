package com.mehmetbozkurt.questlog.data.repository

import com.mehmetbozkurt.questlog.core.common.IoDispatcher
import com.mehmetbozkurt.questlog.core.common.startOfTodayMillis
import com.mehmetbozkurt.questlog.core.database.dao.CatalogDao
import com.mehmetbozkurt.questlog.core.database.entity.CatalogCompletionEntity
import com.mehmetbozkurt.questlog.core.database.entity.CatalogTaskEntity
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.core.sync.SyncScheduler
import com.mehmetbozkurt.questlog.data.remote.CatalogRemoteDataSource
import com.mehmetbozkurt.questlog.domain.model.CatalogEntry
import com.mehmetbozkurt.questlog.domain.model.CatalogTask
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.progression.CatalogRules
import com.mehmetbozkurt.questlog.domain.progression.PathwayRules
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.CatalogCompletionResult
import com.mehmetbozkurt.questlog.domain.repository.CatalogRejection
import com.mehmetbozkurt.questlog.domain.repository.CatalogRepository
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.XpAward
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val dao: CatalogDao,
    private val remote: CatalogRemoteDataSource,
    private val authRepository: AuthRepository,
    private val characterRepository: CharacterRepository,
    private val syncScheduler: SyncScheduler,
    @IoDispatcher private val io: CoroutineDispatcher,
) : CatalogRepository {

    private val completionMutex = Mutex()

    override fun observeCatalog(): Flow<List<CatalogEntry>> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) {
                dao.observeTasks().map { tasks ->
                    tasks.mapNotNull { it.toDomain() }.map { CatalogEntry(it, 0, false) }
                }
            } else {
                combine(
                    dao.observeTasks(),
                    dao.observeCompletions(user.uid),
                ) { tasks, completions ->
                    val todayStart = startOfTodayMillis()
                    val byTask = completions.associateBy { it.taskId }
                    tasks.mapNotNull { entity ->
                        val task = entity.toDomain() ?: return@mapNotNull null
                        val completion = byTask[task.id]
                        CatalogEntry(
                            task = task,
                            completions = completion?.completions ?: 0,
                            doneToday = CatalogRules.isDoneToday(
                                completion?.lastCompletedAtMillis ?: 0L,
                                todayStart,
                            ),
                        )
                    }
                }
            }
        }

    override suspend fun refreshCatalog(): Boolean = withContext(io) {
        if (authRepository.currentUserSync() == null) return@withContext false
        runCatching {
            val tasks = remote.fetchCatalog()
            if (tasks.isNotEmpty()) dao.replaceTasks(tasks)
        }.isSuccess
    }

    override suspend fun completeTask(taskId: String): CatalogCompletionResult =
        withContext(io) {
            completionMutex.withLock {
                val user = authRepository.currentUserSync()
                    ?: return@withLock CatalogCompletionResult.Rejected(CatalogRejection.NO_SESSION)

                val task = dao.getTask(taskId)?.toDomain()
                    ?: return@withLock CatalogCompletionResult.Rejected(
                        CatalogRejection.TASK_NOT_FOUND
                    )

                val todayStart = startOfTodayMillis()
                val existing = dao.getCompletion(user.uid, taskId)

                if (CatalogRules.isDoneToday(existing?.lastCompletedAtMillis ?: 0L, todayStart)) {
                    return@withLock CatalogCompletionResult.Rejected(
                        CatalogRejection.ALREADY_DONE_TODAY
                    )
                }

                if (dao.completionCountSince(user.uid, todayStart) >= CatalogRules.MAX_PER_DAY) {
                    return@withLock CatalogCompletionResult.Rejected(CatalogRejection.DAILY_LIMIT)
                }

                val baseXp = task.difficulty.baseXp
                val split = PathwayRules.splitXp(baseXp)

                val award = characterRepository.awardSplitXp(
                    statType = task.statType,
                    difficulty = task.difficulty,
                    logId = task.id,
                    title = task.title,
                    immediateCharacterXp = split.immediate,
                    fullStatXp = baseXp,
                ) ?: return@withLock CatalogCompletionResult.Rejected(
                    CatalogRejection.XP_NOT_AWARDED
                )

                dao.upsertCompletion(
                    CatalogCompletionEntity(
                        userId = user.uid,
                        taskId = taskId,
                        completions = (existing?.completions ?: 0) + 1,
                        lastCompletedAtMillis = System.currentTimeMillis(),
                        syncState = SyncState.PENDING.name,
                    )
                )

                syncScheduler.requestSync()
                CatalogCompletionResult.Success(award)
            }
        }

    private fun CatalogTaskEntity.toDomain(): CatalogTask? {
        val stat = runCatching { StatType.valueOf(statType) }.getOrNull() ?: return null
        val diff = runCatching { Difficulty.valueOf(difficulty) }.getOrNull() ?: return null
        return CatalogTask(
            id = id,
            title = title,
            description = description,
            titleEn = titleEn,
            descriptionEn = descriptionEn,
            statType = stat,
            difficulty = diff,
            sortOrder = sortOrder,
        )
    }
}
