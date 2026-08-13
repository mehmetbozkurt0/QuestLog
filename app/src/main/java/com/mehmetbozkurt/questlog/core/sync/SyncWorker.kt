package com.mehmetbozkurt.questlog.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.data.remote.QuestLogRemoteDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dao: QuestLogDao,
    private val remote: QuestLogRemoteDataSource
): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val pending = dao.getPendingSync()
        if (pending.isEmpty()) return Result.success()

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