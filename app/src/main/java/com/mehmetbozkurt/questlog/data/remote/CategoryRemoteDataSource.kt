package com.mehmetbozkurt.questlog.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.mehmetbozkurt.questlog.core.database.entity.CategoryEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val collection get() = firestore.collection("categories")

    suspend fun push(entity: CategoryEntity) {
        collection.document(entity.id).set(entity.toFireStoreMap()).await()
    }

    fun observeForUser(uid: String): Flow<List<CategoryEntity>> = callbackFlow {
        val registration = collection
            .whereEqualTo("ownerId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toCategoryEntityOrNull() })
                }
            }
        awaitClose { registration.remove() }
    }
}