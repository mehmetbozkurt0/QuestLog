package com.mehmetbozkurt.questlog.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.mehmetbozkurt.questlog.core.database.entity.QuestLogEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    suspend fun deleteAllOwnedBy(uid: String) {
        collection.whereArrayContains("readerIds", uid).get().await().documents
            .filter { it.getString("ownerId") == uid }
            .forEach { it.reference.delete().await() }
    }

    fun observeForUser(uid: String): Flow<List<QuestLogEntity>> = callbackFlow {
        val registration = collection.whereArrayContains("readerIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toEntityOrNull() })
                }
            }
        awaitClose { registration.remove() }
    }
}