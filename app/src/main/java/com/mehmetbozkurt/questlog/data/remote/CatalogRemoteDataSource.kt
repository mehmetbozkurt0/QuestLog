package com.mehmetbozkurt.questlog.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.mehmetbozkurt.questlog.core.database.entity.CatalogQuestEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val collection get() = firestore.collection("catalogQuests")

    fun observeCatalog(): Flow<List<CatalogQuestEntity>> = callbackFlow {
        val registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.documents.mapNotNull { it.toCatalogEntityOrNull() })
            }
        }
        awaitClose { registration.remove() }
    }
}