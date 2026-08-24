package com.mehmetbozkurt.questlog.core.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mehmetbozkurt.questlog.R

sealed interface GoogleIdTokenResult {
    data class Success(val idToken: String) : GoogleIdTokenResult
    data object Cancelled : GoogleIdTokenResult
    data class Failed(val message: String) : GoogleIdTokenResult
}

object GoogleCredentialProvider {

    suspend fun requestIdToken(context: Context): GoogleIdTokenResult {
        val serverClientId = context.getString(R.string.default_web_client_id)
        val option = GetSignInWithGoogleOption.Builder(serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val response = CredentialManager.create(context).getCredential(context, request)
            val credential = response.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleIdTokenResult.Success(googleCredential.idToken)
            } else {
                GoogleIdTokenResult.Failed("Google kimliği okunamadı.")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleIdTokenResult.Cancelled
        } catch (e: NoCredentialException) {
            Log.e(TAG, "Cihazda Google hesabı yok", e)
            GoogleIdTokenResult.Failed("Cihazda kayıtlı Google hesabı bulunamadı.")
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Google kimliği alınamadı", e)
            GoogleIdTokenResult.Failed("Google ile giriş yapılamadı.")
        }
    }

    private const val TAG = "Renown"
}
