package com.mehmetbozkurt.questlog.core.sync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import com.mehmetbozkurt.questlog.core.common.ApplicationScope
import com.mehmetbozkurt.questlog.core.database.dao.CatalogDao
import com.mehmetbozkurt.questlog.core.database.dao.CharacterDao
import com.mehmetbozkurt.questlog.core.database.dao.CrewDao
import com.mehmetbozkurt.questlog.core.database.dao.PathwayDao
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.data.remote.CharacterRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.CrewRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.PathwayRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.QuestLogRemoteDataSource
import com.mehmetbozkurt.questlog.core.notification.ChatPresence
import com.mehmetbozkurt.questlog.core.settings.SettingsRepository
import com.mehmetbozkurt.questlog.domain.progression.CrewRules
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.CatalogRepository
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.CrewRepository
import com.mehmetbozkurt.questlog.domain.repository.PathwayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteSyncManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val remote: QuestLogRemoteDataSource,
    private val dao: QuestLogDao,
    private val pathwayRemote: PathwayRemoteDataSource,
    private val pathwayDao: PathwayDao,
    private val pathwayRepository: PathwayRepository,
    private val characterRemote: CharacterRemoteDataSource,
    private val characterDao: CharacterDao,
    private val characterRepository: CharacterRepository,
    private val crewRemote: CrewRemoteDataSource,
    private val crewDao: CrewDao,
    private val crewRepository: CrewRepository,
    private val catalogDao: CatalogDao,
    private val catalogRepository: CatalogRepository,
    private val settingsRepository: SettingsRepository,
    private val chatPresence: ChatPresence,
    @ApplicationScope private val scope: CoroutineScope
) {
    fun start(){
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) emptyFlow()
            else remote.observeForUser(user.uid)
        }.retrying("QuestLog")
            .onEach { entities -> dao.mergeFromRemote(entities) }
            .launchIn(scope)

        scope.launch { pathwayRepository.refreshCatalog() }
        scope.launch { catalogRepository.refreshCatalog() }

        authRepository.currentUser
            .flatMapLatest { user ->
                if (user == null) emptyFlow()
                else pathwayRemote.observeProgressForUser(user.uid)
            }
            .retrying("Pathway")
            .onEach { entities -> entities.forEach { pathwayDao.upsertProgress(it) } }
            .launchIn(scope)

        authRepository.currentUser
            .flatMapLatest { user ->
                if (user == null) emptyFlow()
                else characterRemote.observeCharacter(user.uid)
            }
            .retrying("Character")
            .onEach { remoteChar ->
                if (remoteChar != null) {
                    val local = characterDao.getCharacter(remoteChar.userId)
                    if (local == null || remoteChar.updatedAtMillis > local.updatedAtMillis) {
                        characterDao.upsertCharacter(remoteChar)
                    }
                }
            }
            .launchIn(scope)

        authRepository.currentUser
            .flatMapLatest { user ->
                if (user == null) emptyFlow()
                else characterRemote.observeStats(user.uid)
            }
            .retrying("Stats")
            .onEach { remoteStats ->
                remoteStats.forEach { remoteStat ->
                    val local = characterDao.getStat(remoteStat.userId, remoteStat.statType)
                    if (local == null || remoteStat.updatedAtMillis > local.updatedAtMillis) {
                        characterDao.upsertStat(remoteStat)
                    }
                }
            }
            .launchIn(scope)

        authRepository.currentUser
            .flatMapLatest { user ->
                if (user == null) emptyFlow()
                else characterRemote.observeFeats(user.uid)
            }
            .retrying("Feats")
            .onEach { remoteFeats ->
                if (remoteFeats.isNotEmpty()) characterDao.upsertFeats(remoteFeats)
            }
            .launchIn(scope)

        authRepository.currentUser
            .flatMapLatest { user ->
                if (user == null) emptyFlow()
                else characterRemote.observeLedger(user.uid)
            }
            .retrying("Ledger")
            .onEach { remoteEntries ->
                val deleting = characterDao.getPendingDeletionIds().toSet()
                val toUpsert = remoteEntries.filter { it.id !in deleting }
                if (toUpsert.isNotEmpty()) characterDao.upsertLedgerEntries(toUpsert)
            }
            .launchIn(scope)

        authRepository.currentUser
            .flatMapLatest { user ->
                if (user == null) emptyFlow()
                else characterRemote.observeCompletions(user.uid)
            }
            .retrying("Completions")
            .onEach { remoteCompletions ->
                remoteCompletions.forEach { remoteCompletion ->
                    val local = pathwayDao.getCompletion(
                        remoteCompletion.userId,
                        remoteCompletion.questId,
                    )
                    val localPendingNewer = local != null &&
                            local.syncState != SyncState.SYNCED.name &&
                            local.lastCompletedAtMillis >= remoteCompletion.lastCompletedAtMillis
                    if (!localPendingNewer) {
                        pathwayDao.upsertCompletion(remoteCompletion)
                    }
                }
            }
            .launchIn(scope)

        authRepository.currentUser
            .flatMapLatest { user ->
                if (user == null) emptyFlow()
                else characterRemote.observeCatalogCompletions(user.uid)
            }
            .retrying("Catalog completions")
            .onEach { remoteCompletions ->
                remoteCompletions.forEach { remote ->
                    val local = catalogDao.getCompletion(remote.userId, remote.taskId)
                    val localPendingNewer = local != null &&
                            local.syncState != SyncState.SYNCED.name &&
                            local.lastCompletedAtMillis >= remote.lastCompletedAtMillis
                    if (!localPendingNewer) {
                        catalogDao.upsertCompletion(remote)
                    }
                }
            }
            .launchIn(scope)

        authRepository.currentUser
            .flatMapLatest { user ->
                if (user == null) emptyFlow()
                else characterRemote.observeHabitSlots(user.uid)
            }
            .retrying("Habit slots")
            .onEach { remoteSlots ->
                remoteSlots.forEach { remoteSlot ->
                    val local = dao.getSlot(remoteSlot.userId, remoteSlot.slotIndex)
                    if (local == null || remoteSlot.updatedAtMillis > local.updatedAtMillis) {
                        dao.upsertSlot(
                            remoteSlot.copy(
                                lastCompletedDayMillis = maxOf(
                                    remoteSlot.lastCompletedDayMillis,
                                    local?.lastCompletedDayMillis ?: 0L,
                                )
                            )
                        )
                    }
                }
            }
            .launchIn(scope)

        observeCrewId()
            .flatMapLatest { crewId ->
                if (crewId == null) {
                    emptyFlow()
                } else {
                    crewRemote.observeCrew(crewId).catch { cause ->
                        if (cause.isPermissionDenied()) crewRepository.handleEviction()
                        else throw cause
                    }
                }
            }
            .retrying("Crew")
            .onEach { remoteCrew -> if (remoteCrew != null) crewDao.upsertCrew(remoteCrew) }
            .launchIn(scope)

        observeCrewId()
            .flatMapLatest { crewId ->
                if (crewId == null) emptyFlow() else crewRemote.observeMembers(crewId)
            }
            .retrying("Crew members")
            .onEach { remoteMembers ->
                val ownUid = authRepository.currentUserSync()?.uid
                val incoming = remoteMembers.filter { it.userId != ownUid }
                if (incoming.isNotEmpty()) crewDao.upsertMembers(incoming)
            }
            .launchIn(scope)

        observeCrewId()
            .flatMapLatest { crewId ->
                if (crewId == null) emptyFlow() else crewRemote.observeFeed(crewId)
            }
            .retrying("Crew feed")
            .onEach { remoteFeed ->
                val ownUid = authRepository.currentUserSync()?.uid
                remoteFeed.forEach { remoteEntry ->
                    val local = crewDao.getFeedEntry(remoteEntry.id)
                    if (ownUid != null && remoteEntry.authorId == ownUid) {
                        val known = local?.approvedBy.orEmpty().toSet()
                        val fresh = remoteEntry.approvedBy.filter { it != ownUid && it !in known }
                        if (fresh.isNotEmpty()) {
                            characterRepository.awardCharacterOnlyXp(
                                CrewRules.MENTOR_APPROVAL_XP * fresh.size
                            )
                        }
                    }
                    crewDao.upsertFeedEntry(remoteEntry)
                }
            }
            .launchIn(scope)

        observeCrewId()
            .flatMapLatest { crewId ->
                if (crewId == null) emptyFlow() else crewRemote.observeMessages(crewId)
            }
            .retrying("Crew messages")
            .onEach { remoteMessages ->
                if (remoteMessages.isEmpty()) return@onEach
                crewDao.upsertMessages(remoteMessages)

                val ownUid = authRepository.currentUserSync()?.uid
                val lastSeen = settingsRepository.lastSeenCrewMessageMillis()
                val fresh = remoteMessages
                    .filter { it.authorId != ownUid && it.sentAtMillis > lastSeen }
                    .sortedBy { it.sentAtMillis }

                if (fresh.isEmpty()) return@onEach

                if (chatPresence.isChatVisible) {
                    settingsRepository.setLastSeenCrewMessageMillis(fresh.last().sentAtMillis)
                }
            }
            .launchIn(scope)
    }

    private fun Throwable.isPermissionDenied(): Boolean =
        (this as? FirebaseFirestoreException)?.code ==
                FirebaseFirestoreException.Code.PERMISSION_DENIED

    private fun <T> Flow<T>.retrying(label: String): Flow<T> = retryWhen { cause, attempt ->
        Log.e(TAG, "$label sync failed (attempt $attempt), retrying", cause)
        delay((RETRY_BASE_MS shl attempt.coerceAtMost(4).toInt()).coerceAtMost(RETRY_MAX_MS))
        true
    }

    private fun observeCrewId() = authRepository.currentUser
        .flatMapLatest { user ->
            if (user == null) emptyFlow()
            else characterDao.observeCharacter(user.uid).map { it?.crewId }
        }
        .distinctUntilChanged()

    private companion object {
        const val TAG = "QuestLog"
        const val RETRY_BASE_MS = 2000L
        const val RETRY_MAX_MS = 30_000L
    }
}
