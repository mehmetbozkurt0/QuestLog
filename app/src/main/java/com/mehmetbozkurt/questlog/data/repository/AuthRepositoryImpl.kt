package com.mehmetbozkurt.questlog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mehmetbozkurt.questlog.core.common.DataResult
import com.mehmetbozkurt.questlog.core.common.IoDispatcher
import com.mehmetbozkurt.questlog.core.common.runCatchingResult
import com.mehmetbozkurt.questlog.domain.model.AppUser
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.GoogleAuthResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @IoDispatcher
    private val io: CoroutineDispatcher,
): AuthRepository {
    override val currentUser: Flow<AppUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toAppUser())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun currentUserSync(): AppUser? = auth.currentUser?.toAppUser()

    override suspend fun signIn(email: String, password: String): DataResult<AppUser> =
        withContext(io) {
            runCatchingResult {
                val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
                result.user?.toAppUser() ?: error("Kullanıcı bilgisi alınamadı.")
            }
        }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): DataResult<AppUser> = withContext(io) {
        runCatchingResult {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = result.user ?: error("Kullanıcı oluşturulamadı!")

            firebaseUser.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
            ).await()

            val user = AppUser(
                uid = firebaseUser.uid,
                email = firebaseUser.email.orEmpty(),
                displayName = displayName.trim()
            )

            firestore.collection("users").document(user.uid).set(
                mapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "displayName" to user.displayName,
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()

            user
        }
    }

    override suspend fun sendPasswordReset(email: String): DataResult<Unit> =
        withContext(io) {
            runCatchingResult {
                auth.sendPasswordResetEmail(email.trim()).await()
                Unit
            }
        }

    override suspend fun signInWithGoogle(idToken: String): DataResult<GoogleAuthResult> =
        withContext(io) {
            runCatchingResult {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val firebaseUser = result.user ?: error("Kullanıcı bilgisi alınamadı.")
                val isNewUser = result.additionalUserInfo?.isNewUser == true
                val user = firebaseUser.toAppUser()

                val document = mutableMapOf<String, Any>(
                    "uid" to user.uid,
                    "email" to user.email,
                    "displayName" to user.displayName,
                )
                if (isNewUser) {
                    document["createdAt"] = System.currentTimeMillis()
                }

                firestore.collection("users").document(user.uid)
                    .set(document, SetOptions.merge())
                    .await()

                GoogleAuthResult(user = user, isNewUser = isNewUser)
            }
        }

    override fun signOut() = auth.signOut()

    private fun FirebaseUser.toAppUser() = AppUser(
        uid = uid,
        email = email.orEmpty(),
        displayName = displayName.orEmpty().ifBlank { email?.substringBefore("@").orEmpty() },
        photoUrl = photoUrl?.toString(),
    )
}