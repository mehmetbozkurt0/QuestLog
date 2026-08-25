package com.mehmetbozkurt.questlog.data.repository

import com.mehmetbozkurt.questlog.core.common.IoDispatcher
import com.mehmetbozkurt.questlog.core.common.daysAgoMillis
import com.mehmetbozkurt.questlog.core.common.startOfTodayMillis
import com.mehmetbozkurt.questlog.core.database.dao.CharacterDao
import com.mehmetbozkurt.questlog.core.database.dao.CrewDao
import com.mehmetbozkurt.questlog.core.database.entity.CharacterEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewFeedEntity
import com.mehmetbozkurt.questlog.core.database.entity.FeatEntity
import com.mehmetbozkurt.questlog.core.database.entity.PendingDeletionEntity
import com.mehmetbozkurt.questlog.core.database.entity.StatEntity
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.core.database.entity.XpLedgerEntity
import com.mehmetbozkurt.questlog.core.sync.SyncScheduler
import com.mehmetbozkurt.questlog.data.mapper.buildCharacterSheet
import com.mehmetbozkurt.questlog.data.remote.CharacterRemoteDataSource
import com.mehmetbozkurt.questlog.data.mapper.toDomain
import com.mehmetbozkurt.questlog.domain.model.AcquiredFeat
import com.mehmetbozkurt.questlog.domain.model.CharacterSheet
import com.mehmetbozkurt.questlog.domain.model.DayActivity
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.FeatId
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.model.WeeklySummary
import com.mehmetbozkurt.questlog.domain.progression.CrewRules
import com.mehmetbozkurt.questlog.domain.progression.StreakEngine
import com.mehmetbozkurt.questlog.domain.progression.StreakInfo
import com.mehmetbozkurt.questlog.domain.progression.XpContext
import com.mehmetbozkurt.questlog.domain.progression.XpCurve
import com.mehmetbozkurt.questlog.domain.progression.XpEngine
import com.mehmetbozkurt.questlog.domain.progression.XpLimits
import com.mehmetbozkurt.questlog.domain.progression.XpResult
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.LevelUpInfo
import com.mehmetbozkurt.questlog.domain.repository.XpAward
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val dao: CharacterDao,
    private val crewDao: CrewDao,
    private val authRepository: AuthRepository,
    private val syncScheduler: SyncScheduler,
    private val characterRemote: CharacterRemoteDataSource,
    @IoDispatcher private val io: CoroutineDispatcher,
) : CharacterRepository {

    override fun observeCharacter(): Flow<CharacterSheet?> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) {
                flowOf(null)
            } else {
                combine(
                    dao.observeCharacter(user.uid),
                    dao.observeStats(user.uid),
                ) { character, stats ->
                    character?.let { buildCharacterSheet(it, stats) }
                }
            }
        }

    override fun observeFeats(): Flow<List<AcquiredFeat>> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else dao.observeFeats(user.uid).map { list -> list.map { it.toDomain() } }
        }

    override fun observeStreak(): Flow<StreakInfo> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) {
                flowOf(StreakInfo.EMPTY)
            } else {
                combine(
                    dao.observeLedgerTimes(user.uid),
                    dao.observeFeats(user.uid),
                ) { times, feats ->
                    val zone = ZoneId.systemDefault()
                    val activeDays = times
                        .map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
                        .toSet()
                    StreakEngine.calculate(
                        activeDays = activeDays,
                        today = LocalDate.now(zone),
                        hasResolute = feats.any { it.featId == FeatId.RESOLUTE.name },
                    )
                }
            }
        }

    override fun observeWeeklySummary(): Flow<WeeklySummary> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyWeeklySummary())
            } else {
                dao.observeLedgerSince(user.uid, daysAgoMillis(6)).map { entries ->
                    val zone = ZoneId.systemDefault()
                    val today = LocalDate.now(zone)
                    val byDay = entries.groupBy {
                        Instant.ofEpochMilli(it.earnedAtMillis).atZone(zone).toLocalDate()
                    }
                    val days = (6 downTo 0).map { offset ->
                        val date = today.minusDays(offset.toLong())
                        DayActivity(date, byDay[date]?.sumOf { it.finalXp } ?: 0)
                    }
                    val topStat = entries
                        .groupBy { it.statType }
                        .maxByOrNull { (_, list) -> list.sumOf { it.finalXp } }
                        ?.key
                        ?.let { name -> runCatching { StatType.valueOf(name) }.getOrNull() }
                    WeeklySummary(
                        days = days,
                        totalXp = entries.sumOf { it.finalXp },
                        entryCount = entries.size,
                        topStat = topStat,
                    )
                }
            }
        }

    private fun emptyWeeklySummary(): WeeklySummary {
        val today = LocalDate.now()
        return WeeklySummary(
            days = (6 downTo 0).map { DayActivity(today.minusDays(it.toLong()), 0) },
            totalXp = 0,
            entryCount = 0,
            topStat = null,
        )
    }

    private suspend fun streakMilestoneAfterAward(userId: String): Int? {
        if (dao.ledgerCountSince(userId, startOfTodayMillis()) != 1) return null
        val zone = ZoneId.systemDefault()
        val activeDays = dao. getLedgerTimes(userId).map {
            Instant.ofEpochMilli(it).atZone(zone).toLocalDate()
        }.toSet()
        val hasResolute = dao.getFeats(userId).any{
            it.featId == FeatId.RESOLUTE.name
        }
        val streak = StreakEngine.calculate(activeDays, LocalDate.now(zone), hasResolute)
        return streak.currentStreak.takeIf { it in StreakEngine.MILESTONES }
    }

    override suspend fun ensureCharacter() = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext
        if (dao.getCharacter(user.uid) != null) return@withContext

        val fetched = fetchRemoteCharacter(user.uid)
        if (fetched.isFailure) return@withContext

        val remoteCharacter = fetched.getOrNull()
        if (remoteCharacter != null) {
            dao.upsertCharacter(remoteCharacter)
            runCatching { characterRemote.fetchStats(user.uid) }.getOrNull()
                ?.takeIf { it.isNotEmpty() }
                ?.let { dao.upsertStats(it) }
            runCatching { characterRemote.fetchFeats(user.uid) }.getOrNull()
                ?.takeIf { it.isNotEmpty() }
                ?.let { dao.upsertFeats(it) }
            return@withContext
        }

        val now = System.currentTimeMillis()

        dao.upsertCharacter(
            CharacterEntity(
                userId = user.uid,
                totalXp = 0,
                pendingFeatChoices = 0,
                createdAtMillis = now,
                updatedAtMillis = now,
            )
        )

        dao.upsertStats(
            StatType.entries.map { type ->
                StatEntity(
                    userId = user.uid,
                    statType = type.name,
                    value = XpCurve.MIN_STAT,
                    currentXp = 0,
                    updatedAtMillis = now,
                )
            }
        )

        syncScheduler.requestSync()
    }

    private suspend fun fetchRemoteCharacter(uid: String): Result<CharacterEntity?> {
        var attempt = 0
        while (true) {
            val result = runCatching { characterRemote.fetchCharacter(uid) }
            if (result.isSuccess || attempt >= ENSURE_FETCH_ATTEMPTS - 1) return result
            delay(ENSURE_RETRY_BASE_MS shl attempt)
            attempt++
        }
    }

    override suspend fun awardXpFor(log: QuestLog): XpAward? = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext null
        val statType = log.statType ?: return@withContext XpAward.Rejected(
            XpAward.RejectReason.NOT_ELIGIBLE
        )
        val difficulty = log.difficulty ?: return@withContext XpAward.Rejected(
            XpAward.RejectReason.NOT_ELIGIBLE
        )

        ensureCharacter()

        val crewCharacter = dao.getCharacter(user.uid)
        val todayStart = startOfTodayMillis()

        if (XpLimits.ONE_AWARD_PER_LOG_PER_DAY &&
            dao.ledgerCountForLogSince(user.uid, log.id, todayStart) > 0
        ) {
            return@withContext XpAward.Rejected(XpAward.RejectReason.ALREADY_AWARDED_TODAY)
        }

        XpLimits.dailyLimitFor(difficulty)?.let { limit ->
            val used = dao.ledgerCountForDifficultySince(user.uid, difficulty.name, todayStart)
            if (used >= limit) {
                return@withContext XpAward.Rejected(XpAward.RejectReason.DAILY_DIFFICULTY_LIMIT)
            }
        }

        val feats = dao.getFeats(user.uid).map { it.toDomain() }
        val distinctToday = dao.distinctStatsSince(user.uid, todayStart)
            .mapNotNull { runCatching { StatType.valueOf(it) }.getOrNull() }
            .toSet()
        val earnedTodayForStat = dao.xpEarnedForStatSince(user.uid, statType.name, todayStart)

        val completedAt = log.completedAt ?: Instant.now()

        val result = XpEngine.calculate(
            XpContext(
                difficulty = difficulty,
                statType = statType,
                proofLevel = log.proofLevel,
                completedAt = completedAt,
                feats = feats,
                distinctStatsToday = distinctToday,
                xpAlreadyEarnedTodayForStat = earnedTodayForStat,
                isNewMember = crewCharacter?.crewId != null && CrewRules.isNewMember(
                    crewCharacter.crewJoinedAtMillis,
                    completedAt.toEpochMilli(),
                ),
            )
        )

        if (result.finalXp <= 0) {
            return@withContext XpAward.Rejected(XpAward.RejectReason.DAILY_STAT_CAP)
        }

        val now = System.currentTimeMillis()

        val statEntity = dao.getStat(user.uid, statType.name)
            ?: StatEntity(
                userId = user.uid,
                statType = statType.name,
                value = XpCurve.MIN_STAT,
                currentXp = 0,
                updatedAtMillis = now,
            )

        val statUpdate = XpEngine.applyStatXp(
            currentValue = statEntity.value,
            currentXp = statEntity.currentXp,
            gainedXp = result.finalXp,
        )

        dao.upsertStat(
            statEntity.copy(
                value = statUpdate.newValue,
                currentXp = statUpdate.remainingXp,
                updatedAtMillis = now,
                syncState = SyncState.PENDING.name,
            )
        )

        val character = dao.getCharacter(user.uid)!!
        val oldLevel = XpCurve.levelFromTotalXp(character.totalXp).level
        val newTotalXp = character.totalXp + result.finalXp
        val newLevel = XpCurve.levelFromTotalXp(newTotalXp).level
        val featGain = XpCurve.featChoicesBetween(oldLevel, newLevel)

        dao.upsertCharacter(
            character.copy(
                totalXp = newTotalXp,
                pendingFeatChoices = character.pendingFeatChoices + featGain,
                updatedAtMillis = now,
                syncState = SyncState.PENDING.name,
            )
        )

        dao.insertLedger(
            XpLedgerEntity(
                id = UUID.randomUUID().toString(),
                userId = user.uid,
                logId = log.id,
                statType = statType.name, 
                baseXp = result.baseXp,
                finalXp = result.finalXp,
                earnedAtMillis = now,
            )
        )

        val milestone = streakMilestoneAfterAward(user.uid)

        character.crewId?.let { crewId ->
            crewDao.upsertFeedEntry(
                CrewFeedEntity(
                    id = UUID.randomUUID().toString(),
                    crewId = crewId,
                    authorId = user.uid,
                    authorName = user.displayName,
                    questLogId = log.id,
                    title = log.title,
                    statType = statType.name,
                    difficulty = difficulty.name,
                    completedAtMillis = now,
                    proofPhotoUrl = log.proofPhotoUrl,
                )
            )
        }

        syncScheduler.requestSync()

        XpAward.Granted(
            result = result,
            statType = statType,
            statIncreased = statUpdate.increases > 0,
            newStatValue = statUpdate.newValue,
            leveledUp = newLevel > oldLevel,
            newLevel = newLevel,
            featChoicesGained = featGain,
            streakMilestone = milestone
        )
    }

    override suspend fun revokeXpFor(logId: String, sinceMillis: Long?) = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext
        val entries = if (sinceMillis == null) {
            dao.ledgerEntriesForLog(user.uid, logId)
        } else {
            dao.ledgerEntriesForLogSince(user.uid, logId, sinceMillis)
        }
        if (entries.isEmpty()) return@withContext

        val now = System.currentTimeMillis()

        entries.forEach { entry ->
            val statType = runCatching { StatType.valueOf(entry.statType) }.getOrNull()
                ?: return@forEach

            val statEntity = dao.getStat(user.uid, statType.name) ?: return@forEach
            val rollback = XpEngine.removeStatXp(
                currentValue = statEntity.value,
                currentXp = statEntity.currentXp,
                removedXp = entry.finalXp,
            )

            dao.upsertStat(
                statEntity.copy(
                    value = rollback.newValue,
                    currentXp = rollback.remainingXp,
                    updatedAtMillis = now,
                    syncState = SyncState.PENDING.name,
                )
            )
        }

        val totalRemoved = entries.sumOf { it.finalXp }
        val character = dao.getCharacter(user.uid) ?: return@withContext

        dao.upsertCharacter(
            character.copy(
                totalXp = (character.totalXp - totalRemoved).coerceAtLeast(0),
                updatedAtMillis = now,
                syncState = SyncState.PENDING.name,
            )
        )

        dao.insertPendingDeletions(
            entries.map {
                PendingDeletionEntity(
                    docId = it.id,
                    collection = "xpLedger",
                    userId = user.uid,
                )
            }
        )
        dao.deleteLedgerForLog(logId)
        syncScheduler.requestSync()
    }

    override suspend fun chooseFeat(featId: FeatId, chosenStat: StatType?) = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext
        val character = dao.getCharacter(user.uid) ?: return@withContext
        if (character.pendingFeatChoices <= 0) return@withContext

        val now = System.currentTimeMillis()
        val level = XpCurve.levelFromTotalXp(character.totalXp).level

        dao.upsertFeat(
            FeatEntity(
                id = UUID.randomUUID().toString(),
                userId = user.uid,
                featId = featId.name,
                chosenStat = chosenStat?.name,
                acquiredAtLevel = level,
                acquiredAtMillis = now,
            )
        )

        dao.upsertCharacter(
            character.copy(
                pendingFeatChoices = character.pendingFeatChoices - 1,
                updatedAtMillis = now,
                syncState = SyncState.PENDING.name,
            )
        )

        syncScheduler.requestSync()
    }

    override suspend fun awardSplitXp(
        statType: StatType,
        difficulty: Difficulty,
        logId: String,
        title: String,
        immediateCharacterXp: Int,
        fullStatXp: Int,
    ): XpAward.Granted? = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext null
        ensureCharacter()

        val now = System.currentTimeMillis()

        val statEntity = dao.getStat(user.uid, statType.name)
            ?: StatEntity(
                userId = user.uid,
                statType = statType.name,
                value = XpCurve.MIN_STAT,
                currentXp = 0,
                updatedAtMillis = now,
            )

        val statUpdate = XpEngine.applyStatXp(
            currentValue = statEntity.value,
            currentXp = statEntity.currentXp,
            gainedXp = fullStatXp,
        )

        dao.upsertStat(
            statEntity.copy(
                value = statUpdate.newValue,
                currentXp = statUpdate.remainingXp,
                updatedAtMillis = now,
                syncState = SyncState.PENDING.name,
            )
        )

        val character = dao.getCharacter(user.uid) ?: return@withContext null
        val oldLevel = XpCurve.levelFromTotalXp(character.totalXp).level
        val newTotalXp = character.totalXp + immediateCharacterXp
        val newLevel = XpCurve.levelFromTotalXp(newTotalXp).level
        val featGain = XpCurve.featChoicesBetween(oldLevel, newLevel)

        dao.upsertCharacter(
            character.copy(
                totalXp = newTotalXp,
                pendingFeatChoices = character.pendingFeatChoices + featGain,
                updatedAtMillis = now,
                syncState = SyncState.PENDING.name,
            )
        )

        dao.insertLedger(
            XpLedgerEntity(
                id = UUID.randomUUID().toString(),
                userId = user.uid,
                logId = logId,
                statType = statType.name,
                baseXp = difficulty.baseXp,
                finalXp = fullStatXp,
                earnedAtMillis = now,
            )
        )

        val milestone = streakMilestoneAfterAward(user.uid)

        character.crewId?.let { crewId ->
            crewDao.upsertFeedEntry(
                CrewFeedEntity(
                    id = UUID.randomUUID().toString(),
                    crewId = crewId,
                    authorId = user.uid,
                    authorName = user.displayName,
                    questLogId = logId,
                    title = title,
                    statType = statType.name,
                    difficulty = difficulty.name,
                    completedAtMillis = now,
                    proofPhotoUrl = null,
                )
            )
        }

        syncScheduler.requestSync()

        XpAward.Granted(
            result = XpResult(
                baseXp = difficulty.baseXp,
                finalXp = fullStatXp,
                cappedAmount = 0,
                appliedBonuses = emptyList(),
            ),
            statType = statType,
            statIncreased = statUpdate.increases > 0,
            newStatValue = statUpdate.newValue,
            leveledUp = newLevel > oldLevel,
            newLevel = newLevel,
            featChoicesGained = featGain,
            streakMilestone = milestone
        )
    }

    override suspend fun awardCharacterOnlyXp(amount: Int): LevelUpInfo = withContext(io) {
        val user = authRepository.currentUserSync()
            ?: return@withContext LevelUpInfo(false, 1, 0)
        val character = dao.getCharacter(user.uid)
            ?: return@withContext LevelUpInfo(false, 1, 0)

        val now = System.currentTimeMillis()
        val oldLevel = XpCurve.levelFromTotalXp(character.totalXp).level
        val newTotalXp = character.totalXp + amount
        val newLevel = XpCurve.levelFromTotalXp(newTotalXp).level
        val featGain = XpCurve.featChoicesBetween(oldLevel, newLevel)

        dao.upsertCharacter(
            character.copy(
                totalXp = newTotalXp,
                pendingFeatChoices = character.pendingFeatChoices + featGain,
                updatedAtMillis = now,
                syncState = SyncState.PENDING.name,
            )
        )

        syncScheduler.requestSync()

        LevelUpInfo(
            leveledUp = newLevel > oldLevel,
            newLevel = newLevel,
            featChoicesGained = featGain,
        )
    }

    private companion object {
        const val ENSURE_FETCH_ATTEMPTS = 4
        const val ENSURE_RETRY_BASE_MS = 1000L
    }
}