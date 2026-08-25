package com.mehmetbozkurt.questlog.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import com.mehmetbozkurt.questlog.core.common.IoDispatcher
import com.mehmetbozkurt.questlog.core.common.startOfTodayMillis
import com.mehmetbozkurt.questlog.core.database.dao.CharacterDao
import com.mehmetbozkurt.questlog.core.database.dao.CrewDao
import com.mehmetbozkurt.questlog.core.database.entity.CrewEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewFeedEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewMemberEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewMessageEntity
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.core.settings.SettingsRepository
import com.mehmetbozkurt.questlog.core.sync.SyncScheduler
import com.mehmetbozkurt.questlog.data.remote.CrewRemoteDataSource
import com.mehmetbozkurt.questlog.domain.model.Crew
import com.mehmetbozkurt.questlog.domain.model.CrewFeedItem
import com.mehmetbozkurt.questlog.domain.model.CrewMember
import com.mehmetbozkurt.questlog.domain.model.CrewMessage
import com.mehmetbozkurt.questlog.domain.model.CrewState
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.FeatId
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.progression.CrewRules
import com.mehmetbozkurt.questlog.domain.progression.XpCurve
import com.mehmetbozkurt.questlog.domain.repository.ApproveFailure
import com.mehmetbozkurt.questlog.domain.repository.ApproveResult
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.CrewActionResult
import com.mehmetbozkurt.questlog.domain.repository.CrewFailure
import com.mehmetbozkurt.questlog.domain.repository.CrewRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrewRepositoryImpl @Inject constructor(
    private val crewDao: CrewDao,
    private val characterDao: CharacterDao,
    private val authRepository: AuthRepository,
    private val characterRepository: CharacterRepository,
    private val crewRemote: CrewRemoteDataSource,
    private val settingsRepository: SettingsRepository,
    private val syncScheduler: SyncScheduler,
    @IoDispatcher private val io: CoroutineDispatcher,
) : CrewRepository {

    override fun observeCrewState(): Flow<CrewState> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) {
                flowOf(CrewState.EMPTY)
            } else {
                characterDao.observeCharacter(user.uid).flatMapLatest { character ->
                    val crewId = character?.crewId
                    if (crewId == null) {
                        flowOf(CrewState.EMPTY)
                    } else {
                        combine(
                            crewDao.observeCrew(crewId),
                            crewDao.observeMembers(crewId),
                            crewDao.observeFeed(crewId, FEED_PAGE),
                        ) { crew, members, feed ->
                            CrewState(
                                crew = crew?.toDomain(),
                                members = members.map { it.toDomain() },
                                feed = feed.map { it.toDomain() },
                            )
                        }
                    }
                }
            }
        }

    override fun observeMessages(): Flow<List<CrewMessage>> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                characterDao.observeCharacter(user.uid).flatMapLatest { character ->
                    val crewId = character?.crewId
                    if (crewId == null) {
                        flowOf(emptyList())
                    } else {
                        crewDao.observeMessages(crewId, MESSAGE_PAGE)
                            .map { messages -> messages.map { it.toDomain() } }
                    }
                }
            }
        }

    override fun observeUnreadMessageCount(): Flow<Int> = combine(
        observeMessages(),
        settingsRepository.observeLastSeenCrewMessageMillis(),
        authRepository.currentUser,
    ) { messages, lastSeen, user ->
        messages.count { it.authorId != user?.uid && it.sentAt.toEpochMilli() > lastSeen }
    }

    override suspend fun sendMessage(text: String): CrewActionResult = withContext(io) {
        val trimmed = text.trim().take(CrewRules.MESSAGE_MAX_LENGTH)
        if (trimmed.isEmpty()) return@withContext CrewActionResult.Success

        val user = authRepository.currentUserSync()
            ?: return@withContext CrewActionResult.Failed(CrewFailure.NO_SESSION)
        val character = characterDao.getCharacter(user.uid)
            ?: return@withContext CrewActionResult.Failed(CrewFailure.NO_CHARACTER)
        val crewId = character.crewId ?: return@withContext CrewActionResult.NotInCrew

        val entity = CrewMessageEntity(
            id = UUID.randomUUID().toString(),
            crewId = crewId,
            authorId = user.uid,
            authorName = user.displayName,
            text = trimmed,
            sentAtMillis = System.currentTimeMillis(),
        )
        crewDao.upsertMessage(entity)

        val pushed = withTimeoutOrNull(PUSH_TIMEOUT_MS) {
            runCatching { crewRemote.pushMessage(entity) }.isSuccess
        } ?: false

        if (pushed) {
            crewDao.upsertMessage(entity.copy(syncState = SyncState.SYNCED.name))
        } else {
            syncScheduler.requestSync()
        }
        CrewActionResult.Success
    }

    override suspend fun createCrew(name: String): CrewActionResult = withContext(io) {
        val user = authRepository.currentUserSync()
            ?: return@withContext CrewActionResult.Failed(CrewFailure.NO_SESSION)
        val character = characterDao.getCharacter(user.uid)
            ?: return@withContext CrewActionResult.Failed(CrewFailure.NO_CHARACTER)
        if (character.crewId != null) return@withContext CrewActionResult.AlreadyInCrew

        val now = System.currentTimeMillis()
        val entity = CrewEntity(
            crewId = UUID.randomUUID().toString(),
            name = name.trim(),
            inviteCode = CrewRules.generateInviteCode(),
            ownerId = user.uid,
            memberIdsCsv = user.uid,
            updatedAtMillis = now,
        )

        runCatching { crewRemote.createCrew(entity) }
            .onFailure { return@withContext it.toCrewFailure("create") }

        crewDao.upsertCrew(entity)
        characterDao.upsertCharacter(
            character.copy(
                crewId = entity.crewId,
                crewJoinedAtMillis = character.crewJoinedAtMillis ?: now,
                updatedAtMillis = now,
                syncState = SyncState.PENDING.name,
            )
        )
        refreshMemberCard()
        syncScheduler.requestSync()
        CrewActionResult.Success
    }

    override suspend fun joinByCode(code: String): CrewActionResult = withContext(io) {
        val user = authRepository.currentUserSync()
            ?: return@withContext CrewActionResult.Failed(CrewFailure.NO_SESSION)
        val character = characterDao.getCharacter(user.uid)
            ?: return@withContext CrewActionResult.Failed(CrewFailure.NO_CHARACTER)
        if (character.crewId != null) return@withContext CrewActionResult.AlreadyInCrew

        val normalized = code.trim().uppercase()
        val crewId = runCatching { crewRemote.findCrewIdByCode(normalized) }
            .getOrElse { return@withContext it.toCrewFailure("code lookup") }
            ?: return@withContext CrewActionResult.CodeNotFound

        runCatching { crewRemote.joinCrew(crewId, user.uid) }
            .onFailure { return@withContext it.toCrewFailure("join") }

        val remoteCrew = runCatching { crewRemote.fetchCrew(crewId) }.getOrNull()
        if (remoteCrew != null) crewDao.upsertCrew(remoteCrew)

        val now = System.currentTimeMillis()
        characterDao.upsertCharacter(
            character.copy(
                crewId = crewId,
                crewJoinedAtMillis = character.crewJoinedAtMillis ?: now,
                updatedAtMillis = now,
                syncState = SyncState.PENDING.name,
            )
        )
        refreshMemberCard()
        syncScheduler.requestSync()
        CrewActionResult.Success
    }

    override suspend fun leaveCrew(): CrewActionResult = withContext(io) {
        val user = authRepository.currentUserSync()
            ?: return@withContext CrewActionResult.Failed(CrewFailure.NO_SESSION)
        val character = characterDao.getCharacter(user.uid)
            ?: return@withContext CrewActionResult.Failed(CrewFailure.NO_CHARACTER)
        val crewId = character.crewId ?: return@withContext CrewActionResult.NotInCrew

        runCatching { crewRemote.leaveCrew(crewId, user.uid) }
            .onFailure { return@withContext it.toCrewFailure("leave") }

        crewDao.deleteFeedForCrew(crewId)
        crewDao.deleteMessagesForCrew(crewId)
        crewDao.deleteMembersForCrew(crewId)
        crewDao.deleteCrew(crewId)
        characterDao.upsertCharacter(
            character.copy(
                crewId = null,
                updatedAtMillis = System.currentTimeMillis(),
                syncState = SyncState.PENDING.name,
            )
        )
        syncScheduler.requestSync()
        CrewActionResult.Success
    }

    override suspend fun approve(entryId: String): ApproveResult = withContext(io) {
        val user = authRepository.currentUserSync()
            ?: return@withContext ApproveResult.Failed(ApproveFailure.NO_SESSION)
        val character = characterDao.getCharacter(user.uid)
            ?: return@withContext ApproveResult.Failed(ApproveFailure.NO_CHARACTER)
        val crewId = character.crewId
            ?: return@withContext ApproveResult.Failed(ApproveFailure.NOT_IN_CREW)

        val hasMentor = characterDao.getFeats(user.uid).any { it.featId == FeatId.MENTOR.name }
        if (!hasMentor) return@withContext ApproveResult.NoMentorFeat

        val entry = crewDao.getFeedEntry(entryId)
            ?: return@withContext ApproveResult.Failed(ApproveFailure.ENTRY_NOT_FOUND)
        if (entry.authorId == user.uid) return@withContext ApproveResult.OwnQuest
        if (user.uid in entry.approvedBy) return@withContext ApproveResult.AlreadyApproved

        val today = startOfTodayMillis()
        val usedToday =
            if (character.approvalDayMillis == today) character.approvalsToday else 0
        if (usedToday >= CrewRules.DAILY_APPROVAL_LIMIT) {
            return@withContext ApproveResult.DailyLimitReached
        }

        runCatching { crewRemote.approveFeedEntry(crewId, entryId, user.uid) }
            .onFailure {
                Log.e(TAG, "Crew approve failed", it)
                return@withContext ApproveResult.Failed(ApproveFailure.WRITE_FAILED)
            }

        crewDao.upsertFeedEntry(
            entry.copy(approvedByCsv = (entry.approvedBy + user.uid).joinToString(","))
        )
        characterDao.upsertCharacter(
            character.copy(
                approvalDayMillis = today,
                approvalsToday = usedToday + 1,
                updatedAtMillis = System.currentTimeMillis(),
                syncState = SyncState.PENDING.name,
            )
        )

        val levelUp = characterRepository.awardCharacterOnlyXp(CrewRules.MENTOR_APPROVAL_XP)
        ApproveResult.Granted(
            xp = CrewRules.MENTOR_APPROVAL_XP,
            leveledUp = levelUp.leveledUp,
            newLevel = levelUp.newLevel,
        )
    }

    override suspend fun refreshMemberCard() = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext
        val character = characterDao.getCharacter(user.uid) ?: return@withContext
        val crewId = character.crewId ?: return@withContext
        val streak = characterRepository.observeStreak().first()

        crewDao.upsertMember(
            CrewMemberEntity(
                userId = user.uid,
                crewId = crewId,
                displayName = user.displayName,
                level = XpCurve.levelFromTotalXp(character.totalXp).level,
                totalXp = character.totalXp,
                currentStreak = streak.currentStreak,
                updatedAtMillis = System.currentTimeMillis(),
                syncState = SyncState.PENDING.name,
            )
        )
        syncScheduler.requestSync()
    }

    private fun CrewEntity.toDomain() = Crew(
        crewId = crewId,
        name = name,
        inviteCode = inviteCode,
        ownerId = ownerId,
        memberIds = memberIds,
    )

    private fun CrewMemberEntity.toDomain() = CrewMember(
        userId = userId,
        displayName = displayName,
        level = level,
        totalXp = totalXp,
        currentStreak = currentStreak,
    )

    private fun CrewFeedEntity.toDomain() = CrewFeedItem(
        id = id,
        authorId = authorId,
        authorName = authorName,
        title = title,
        statType = statType?.let { runCatching { StatType.valueOf(it) }.getOrNull() },
        difficulty = difficulty?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() },
        completedAt = Instant.ofEpochMilli(completedAtMillis),
        proofPhotoUrl = proofPhotoUrl,
        approvedBy = approvedBy,
    )

    private fun CrewMessageEntity.toDomain() = CrewMessage(
        id = id,
        authorId = authorId,
        authorName = authorName,
        text = text,
        sentAt = Instant.ofEpochMilli(sentAtMillis),
        isPending = syncState != SyncState.SYNCED.name,
    )

    private fun Throwable.toCrewFailure(action: String): CrewActionResult {
        Log.e(TAG, "Crew $action failed", this)
        return when ((this as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                CrewActionResult.Failed(CrewFailure.PERMISSION_DENIED)

            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                -> CrewActionResult.Offline

            else -> CrewActionResult.Failed(CrewFailure.UNKNOWN)
        }
    }

    companion object {
        private const val FEED_PAGE = 50
        private const val MESSAGE_PAGE = 200
        private const val PUSH_TIMEOUT_MS = 5000L
        private const val TAG = "QuestLog"
    }
}
