package com.mehmetbozkurt.questlog.core.sync

import android.util.Log
import com.mehmetbozkurt.questlog.core.common.ApplicationScope
import com.mehmetbozkurt.questlog.core.database.dao.CharacterDao
import com.mehmetbozkurt.questlog.core.database.dao.PathwayDao
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.data.remote.CharacterRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.PathwayRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.QuestLogRemoteDataSource
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.PathwayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
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
    }

    private companion object {
        const val TAG = "QuestLog"
    }
}
