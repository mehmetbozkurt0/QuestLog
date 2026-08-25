package com.mehmetbozkurt.questlog.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.mehmetbozkurt.questlog.core.database.dao.CatalogDao
import com.mehmetbozkurt.questlog.core.database.dao.CharacterDao
import com.mehmetbozkurt.questlog.core.database.dao.CrewDao
import com.mehmetbozkurt.questlog.core.database.dao.PathwayDao
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.data.remote.CharacterRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.CrewRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.PathwayRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.ProofPhotoRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.QuestLogRemoteDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dao: QuestLogDao,
    private val remote: QuestLogRemoteDataSource,
    private val pathwayDao: PathwayDao,
    private val pathwayRemote: PathwayRemoteDataSource,
    private val characterDao: CharacterDao,
    private val characterRemote: CharacterRemoteDataSource,
    private val crewDao: CrewDao,
    private val catalogDao: CatalogDao,
    private val crewRemote: CrewRemoteDataSource,
    private val proofPhotoRemote: ProofPhotoRemoteDataSource,
): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        var hadRetryableFailure = false

        dao.getPendingProofPhotos().forEach { entity ->
            val localPath = entity.proofPhotoLocalPath ?: return@forEach
            try {
                val url = proofPhotoRemote.upload(entity.ownerId, entity.id, localPath)
                dao.setProofPhotoUrl(entity.id, url)
                dao.updateSyncState(entity.id, SyncState.PENDING.name)
                crewDao.setFeedProofPhotoUrl(entity.id, entity.ownerId, url)
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
            }
        }

        dao.getPendingSync().forEach { entity ->
            try {
                remote.push(entity)
                dao.updateSyncState(entity.id, SyncState.SYNCED.name)
            } catch (e: Exception) {
                if (e.isRetryable()) {
                    hadRetryableFailure = true
                } else  {
                    dao.updateSyncState(entity.id, SyncState.FAILED.name)
                }
            }
        }

        pathwayDao.getPendingProgress().forEach { entity ->
            try {
                pathwayRemote.pushProgress(entity)
                pathwayDao.upsertProgress(entity.copy(syncState = SyncState.SYNCED.name))
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
            }
        }

        pathwayDao.getPendingCompletions().forEach { entity ->
            try {
                characterRemote.pushCompletion(entity)
                pathwayDao.upsertCompletion(entity.copy(syncState = SyncState.SYNCED.name))
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
            }
        }

        catalogDao.getPendingCompletions().forEach { entity ->
            try {
                characterRemote.pushCatalogCompletion(entity)
                catalogDao.upsertCompletion(entity.copy(syncState = SyncState.SYNCED.name))
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
            }
        }

        dao.getPendingSlots().forEach { entity ->
            try {
                characterRemote.pushHabitSlot(entity)
                dao.upsertSlot(entity.copy(syncState = SyncState.SYNCED.name))
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
            }
        }

        characterDao.getPendingCharacters().forEach { entity ->
            try {
                characterRemote.pushCharacter(entity)
                characterDao.upsertCharacter(entity.copy(syncState = SyncState.SYNCED.name))
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
            }
        }

        characterDao.getPendingStats().forEach { entity ->
            try {
                characterRemote.pushStat(entity)
                characterDao.upsertStat(entity.copy(syncState = SyncState.SYNCED.name))
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
            }
        }

        characterDao.getPendingFeats().forEach { entity ->
            try {
                characterRemote.pushFeat(entity)
                characterDao.upsertFeat(entity.copy(syncState = SyncState.SYNCED.name))
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
            }
        }

        characterDao.getPendingLedger().forEach { entity ->
            try {
                characterRemote.pushLedgerEntry(entity)
                characterDao.insertLedger(entity.copy(syncState = SyncState.SYNCED.name))
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
            }
        }

        characterDao.getPendingDeletions().forEach { deletion ->
            try {
                characterRemote.deleteLedgerEntry(deletion.userId, deletion.docId)
                characterDao.clearPendingDeletion(deletion.docId)
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
            }
        }

        crewDao.getPendingMembers().forEach { entity ->
            try {
                crewRemote.pushMemberCard(entity)
                crewDao.upsertMember(entity.copy(syncState = SyncState.SYNCED.name))
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
            }
        }

        crewDao.getPendingFeedEntries().forEach { entity ->
            try {
                crewRemote.pushFeedEntry(entity)
                crewDao.upsertFeedEntry(entity.copy(syncState = SyncState.SYNCED.name))
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
            }
        }

        crewDao.getPendingMessages().forEach { entity ->
            try {
                crewRemote.pushMessage(entity)
                crewDao.upsertMessage(entity.copy(syncState = SyncState.SYNCED.name))
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
            }
        }

        return if (hadRetryableFailure) Result.retry() else Result.success()
    }

    private fun Exception.isRetryable(): Boolean = when(this) {
        is FirebaseNetworkException -> true
        is FirebaseFirestoreException -> code in RETRYABLE_CODES
        else -> true
    }

    companion object {
        private val RETRYABLE_CODES = setOf(
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED,
            FirebaseFirestoreException.Code.ABORTED,
            FirebaseFirestoreException.Code.INTERNAL,
        )

        const val WORK_NAME = "questlog_sync"
    }
}