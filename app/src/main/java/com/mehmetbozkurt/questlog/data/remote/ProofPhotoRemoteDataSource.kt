package com.mehmetbozkurt.questlog.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProofPhotoRemoteDataSource @Inject constructor(
    private val storage: FirebaseStorage,
) {
    suspend fun upload(userId: String, logId: String, localPath: String): String {
        val file = File(localPath)
        val ref = storage.reference.child("proofs/$userId/$logId.jpg")
        ref.putFile(Uri.fromFile(file)).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun delete(userId: String, logId: String) {
        runCatching {
            storage.reference.child("proofs/$userId/$logId.jpg").delete().await()
        }
    }
}
