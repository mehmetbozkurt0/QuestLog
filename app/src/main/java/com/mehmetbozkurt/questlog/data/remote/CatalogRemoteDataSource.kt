package com.mehmetbozkurt.questlog.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.mehmetbozkurt.questlog.core.database.entity.CatalogTaskEntity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val collection get() = firestore.collection("taskCatalog")

    suspend fun fetchCatalog(): List<CatalogTaskEntity> =
        collection.get().await().documents.mapNotNull { it.toCatalogTaskEntityOrNull() }
}
