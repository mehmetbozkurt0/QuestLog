package com.mehmetbozkurt.questlog.data.repository

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.mehmetbozkurt.questlog.core.common.IoDispatcher
import com.mehmetbozkurt.questlog.core.database.QuestLogDatabase
import com.mehmetbozkurt.questlog.core.database.dao.CharacterDao
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.core.media.ProofPhotoStore
import com.mehmetbozkurt.questlog.data.remote.CharacterRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.CrewRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.PathwayRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.ProofPhotoRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.QuestLogRemoteDataSource
import com.mehmetbozkurt.questlog.domain.repository.AccountRepository
import com.mehmetbozkurt.questlog.domain.repository.DeleteAccountResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: QuestLogDatabase,
    private val questLogDao: QuestLogDao,
    private val characterDao: CharacterDao,
    private val questLogRemote: QuestLogRemoteDataSource,
    private val characterRemote: CharacterRemoteDataSource,
    private val pathwayRemote: PathwayRemoteDataSource,
    private val crewRemote: CrewRemoteDataSource,
    private val proofPhotoRemote: ProofPhotoRemoteDataSource,
    private val photoStore: ProofPhotoStore,
    @IoDispatcher private val io: CoroutineDispatcher,
) : AccountRepository {

    override fun isPasswordAccount(): Boolean =
        auth.currentUser?.providerData?.any { it.providerId == "password" } == true

    override suspend fun deleteAccount(password: String): DeleteAccountResult =
        withContext(io) {
            val user = auth.currentUser ?: return@withContext DeleteAccountResult.NoSession
            val email = user.email ?: return@withContext DeleteAccountResult.NoSession

            try {
                user.reauthenticate(EmailAuthProvider.getCredential(email, password)).await()
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.e(TAG, "Yeniden kimlik doğrulama reddedildi", e)
                return@withContext DeleteAccountResult.WrongPassword
            } catch (e: Exception) {
                Log.e(TAG, "Yeniden kimlik doğrulama başarısız", e)
                return@withContext DeleteAccountResult.Failed(
                    e.message ?: "Kimlik doğrulanamadı."
                )
            }

            wipeAndDelete(user)
        }

    override suspend fun deleteAccountWithGoogle(idToken: String): DeleteAccountResult =
        withContext(io) {
            val user = auth.currentUser ?: return@withContext DeleteAccountResult.NoSession

            try {
                user.reauthenticate(GoogleAuthProvider.getCredential(idToken, null)).await()
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.e(TAG, "Google yeniden kimlik doğrulaması reddedildi", e)
                return@withContext DeleteAccountResult.WrongPassword
            } catch (e: Exception) {
                Log.e(TAG, "Google yeniden kimlik doğrulaması başarısız", e)
                return@withContext DeleteAccountResult.Failed(
                    e.message ?: "Kimlik doğrulanamadı."
                )
            }

            wipeAndDelete(user)
        }

    private suspend fun wipeAndDelete(user: FirebaseUser): DeleteAccountResult {
        val uid = user.uid
        return try {
            deleteProofPhotos(uid)
            deleteCrewTraces(uid)
            questLogRemote.deleteAllOwnedBy(uid)
            pathwayRemote.deleteProgressForUser(uid)
            characterRemote.deleteUserDocument(uid)
            database.clearAllTables()
            user.delete().await()
            DeleteAccountResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Hesap silinemedi", e)
            DeleteAccountResult.Failed(e.message ?: "Hesap silinemedi.")
        }
    }

    private suspend fun deleteProofPhotos(uid: String) {
        questLogDao.getAllForOwner(uid).forEach { log ->
            photoStore.delete(log.proofPhotoLocalPath)
            if (log.proofPhotoUrl != null) {
                proofPhotoRemote.delete(uid, log.id)
            }
        }
    }

    private suspend fun deleteCrewTraces(uid: String) {
        val crewId = characterDao.getCharacter(uid)?.crewId ?: return
        runCatching { crewRemote.deleteFeedEntriesBy(crewId, uid) }
        runCatching { crewRemote.leaveCrew(crewId, uid) }
    }

    private companion object {
        const val TAG = "Renown"
    }
}
