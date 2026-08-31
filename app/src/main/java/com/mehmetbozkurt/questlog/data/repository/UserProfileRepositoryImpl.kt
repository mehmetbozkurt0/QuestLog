package com.mehmetbozkurt.questlog.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.mehmetbozkurt.questlog.core.common.IoDispatcher
import com.mehmetbozkurt.questlog.core.media.AvatarStore
import com.mehmetbozkurt.questlog.data.remote.AvatarRemoteDataSource
import com.mehmetbozkurt.questlog.domain.repository.CrewRepository
import com.mehmetbozkurt.questlog.domain.repository.ProfileRules
import com.mehmetbozkurt.questlog.domain.repository.ProfileUpdateResult
import com.mehmetbozkurt.questlog.domain.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val avatarStore: AvatarStore,
    private val avatarRemote: AvatarRemoteDataSource,
    private val crewRepository: CrewRepository,
    @IoDispatcher private val io: CoroutineDispatcher,
) : UserProfileRepository {

    override suspend fun updateDisplayName(name: String): ProfileUpdateResult = withContext(io) {
        val user = auth.currentUser ?: return@withContext ProfileUpdateResult.NoSession
        val trimmed = name.trim()
        if (trimmed.length < ProfileRules.NAME_MIN_LENGTH) {
            return@withContext ProfileUpdateResult.NameTooShort
        }
        val clipped = trimmed.take(ProfileRules.NAME_MAX_LENGTH)

        try {
            user.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(clipped).build()
            ).await()

            firestore.collection("users").document(user.uid)
                .set(mapOf("displayName" to clipped), SetOptions.merge())
                .await()

            crewRepository.refreshMemberCard()
            ProfileUpdateResult.Success
        } catch (e: Exception) {
            e.toResult("display name")
        }
    }

    override suspend fun updateAvatarFromUri(source: Uri): ProfileUpdateResult = withContext(io) {
        val path = avatarStore.importFromUri(source)
            ?: return@withContext ProfileUpdateResult.ImageUnreadable
        publishAvatar(path)
    }

    override suspend fun updateAvatarFromFile(file: File): ProfileUpdateResult = withContext(io) {
        val path = avatarStore.importFromFile(file)
            ?: return@withContext ProfileUpdateResult.ImageUnreadable
        publishAvatar(path)
    }

    override suspend fun removeAvatar(): ProfileUpdateResult = withContext(io) {
        val user = auth.currentUser ?: return@withContext ProfileUpdateResult.NoSession
        try {
            avatarRemote.delete(user.uid)
            avatarStore.delete()
            writePhotoUrl(null)
            ProfileUpdateResult.Success
        } catch (e: Exception) {
            e.toResult("avatar remove")
        }
    }

    private suspend fun publishAvatar(localPath: String): ProfileUpdateResult {
        val user = auth.currentUser ?: return ProfileUpdateResult.NoSession
        return try {
            val url = avatarRemote.upload(user.uid, localPath)
            writePhotoUrl(url)
            ProfileUpdateResult.Success
        } catch (e: Exception) {
            e.toResult("avatar upload")
        }
    }

    private suspend fun writePhotoUrl(url: String?) {
        val user = auth.currentUser ?: return
        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setPhotoUri(url?.let { Uri.parse(it) })
                .build()
        ).await()

        firestore.collection("users").document(user.uid)
            .set(mapOf("photoUrl" to url), SetOptions.merge())
            .await()

        crewRepository.refreshMemberCard()
    }

    private fun Throwable.toResult(action: String): ProfileUpdateResult {
        Log.e(TAG, "Profile $action failed", this)
        val firestoreCode = (this as? FirebaseFirestoreException)?.code
        return when {
            firestoreCode == FirebaseFirestoreException.Code.UNAVAILABLE ||
                    firestoreCode == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ||
                    this is IOException -> ProfileUpdateResult.Offline

            else -> ProfileUpdateResult.Failed
        }
    }

    private companion object {
        const val TAG = "Renown"
    }
}
