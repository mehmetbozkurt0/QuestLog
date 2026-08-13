package com.mehmetbozkurt.questlog.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.mehmetbozkurt.questlog.core.database.entity.QuestLogEntity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestLogRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection get() = firestore.collection("questLogs")

    suspend fun push(entity: QuestLogEntity) {
        collection.document(entity.id).set(entity.toFireStoreMap()).await()
    }
}