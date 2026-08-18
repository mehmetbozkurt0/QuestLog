package com.mehmetbozkurt.questlog.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.mehmetbozkurt.questlog.core.database.dao.CategoryDao
import com.mehmetbozkurt.questlog.core.database.dao.PathwayDao
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.data.remote.CategoryRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.PathwayRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.QuestLogRemoteDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dao: QuestLogDao,
    private val remote: QuestLogRemoteDataSource,
    private val categoryDao: CategoryDao,
    private val categoryRemote: CategoryRemoteDataSource,
    private val pathwayDao: PathwayDao,
    private val pathwayRemote: PathwayRemoteDataSource,
): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val pending = dao.getPendingSync()
        if (pending.isEmpty() &&
            categoryDao.getPendingSync().isEmpty() &&
            pathwayDao.getPendingProgress().isEmpty()
        ) {
            return Result.success()
        }

        var hadRetryableFailure = false

        pending.forEach { entity ->
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

        val pendingCategories = categoryDao.getPendingSync()
        pendingCategories.forEach { entity ->
            try {
                categoryRemote.push(entity)
                categoryDao.updateSyncState(entity.id, SyncState.SYNCED.name)
            } catch (e: Exception) {
                if (e.isRetryable()) hadRetryableFailure = true
                else categoryDao.updateSyncState(entity.id, SyncState.FAILED.name)
            }
        }

        val pendingPathways = pathwayDao.getPendingProgress()
        pendingPathways.forEach { entity ->
            try {
                pathwayRemote.pushProgress(entity)
                pathwayDao.upsertProgress(entity.copy(syncState = SyncState.SYNCED.name))
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