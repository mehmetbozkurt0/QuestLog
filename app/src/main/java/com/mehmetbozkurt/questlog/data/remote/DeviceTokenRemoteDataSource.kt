package com.mehmetbozkurt.questlog.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceTokenRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private fun tokens(uid: String) =
        firestore.collection("users").document(uid).collection("tokens")

    suspend fun put(uid: String, token: String) {
        tokens(uid).document(token).set(
            mapOf(
                "token" to token,
                "platform" to "android",
                "updatedAtMillis" to System.currentTimeMillis(),
            )
        ).await()
    }

    suspend fun remove(uid: String, token: String) {
        tokens(uid).document(token).delete().await()
    }

    suspend fun removeAll(uid: String) {
        tokens(uid).get().await().documents.forEach { it.reference.delete().await() }
    }
}
