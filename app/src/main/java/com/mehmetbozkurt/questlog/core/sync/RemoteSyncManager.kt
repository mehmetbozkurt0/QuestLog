package com.mehmetbozkurt.questlog.core.sync

import android.util.Log
import com.mehmetbozkurt.questlog.core.common.ApplicationScope
import com.mehmetbozkurt.questlog.core.database.dao.CharacterDao
import com.mehmetbozkurt.questlog.core.database.dao.CrewDao
import com.mehmetbozkurt.questlog.core.database.dao.PathwayDao
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.data.remote.CharacterRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.CrewRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.PathwayRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.QuestLogRemoteDataSource
import com.mehmetbozkurt.questlog.domain.progression.CrewRules
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.PathwayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
    @ApplicationScope private val scope: CoroutineScope
) {
    fun start(){
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) emptyFlow()
            else remote.observeForUser(user.uid)
        }.catch { e -> Log.e(TAG, "QuestLog sync", e) }
            .onEach { entities -> dao.mergeFromRemote(entities) }
            .launchIn(scope)

        scope.launch { pathwayRepository.refreshCatalog() }

        authRepository.currentUser
            .flatMapLatest { user ->
                if (user == null) emptyFlow()
                else pathwayRemote.observeProgressForUser(user.uid)
            }
            .catch { e -> Log.e(TAG, "Pathway sync", e) }
            .onEach { entities -> entities.forEach { pathwayDao.upsertProgress(it) } }
            .launchIn(scope)

        authRepository.currentUser
            .flatMapLatest { user ->
                if (user == null) emptyFlow()
                else characterRemote.observeCharacter(user.uid)
            }
            .catch { e -> Log.e(TAG, "Character sync", e) }
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
            .catch { e -> Log.e(TAG, "Stats sync", e) }
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
            .catch { e -> Log.e(TAG, "Feats sync", e) }
            .onEach { remoteFeats ->
                if (remoteFeats.isNotEmpty()) characterDao.upsertFeats(remoteFeats)
            }
            .launchIn(scope)

        authRepository.currentUser
            .flatMapLatest { user ->
                if (user == null) emptyFlow()
                else characterRemote.observeLedger(user.uid)
            }
            .catch { e -> Log.e(TAG, "Ledger sync", e) }
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
            .catch { e -> Log.e(TAG, "Completions sync", e) }
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

        observeCrewId()
            .flatMapLatest { crewId ->
                if (crewId == null) emptyFlow() else crewRemote.observeCrew(crewId)
            }
            .catch { e -> Log.e(TAG, "Crew sync", e) }
            .onEach { remoteCrew -> if (remoteCrew != null) crewDao.upsertCrew(remoteCrew) }
            .launchIn(scope)

        observeCrewId()
            .flatMapLatest { crewId ->
                if (crewId == null) emptyFlow() else crewRemote.observeMembers(crewId)
            }
            .catch { e -> Log.e(TAG, "Crew members sync", e) }
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
            .catch { e -> Log.e(TAG, "Crew feed sync", e) }
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
    }

    private fun observeCrewId() = authRepository.currentUser
        .flatMapLatest { user ->
            if (user == null) emptyFlow()
            else characterDao.observeCharacter(user.uid).map { it?.crewId }
        }
        .distinctUntilChanged()

    private companion object {
        const val TAG = "QuestLog"
    }
}
