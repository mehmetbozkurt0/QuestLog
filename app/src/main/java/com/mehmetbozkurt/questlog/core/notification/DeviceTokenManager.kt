package com.mehmetbozkurt.questlog.core.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.mehmetbozkurt.questlog.data.remote.DeviceTokenRemoteDataSource
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceTokenManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val remote: DeviceTokenRemoteDataSource,
) {
    suspend fun register(token: String? = null) {
        val uid = authRepository.currentUserSync()?.uid ?: return
        val value = token ?: runCatching {
            FirebaseMessaging.getInstance().token.await()
        }.getOrElse {
            Log.e(TAG, "FCM jetonu alınamadı", it)
            return
        }
        runCatching { remote.put(uid, value) }
            .onFailure { Log.e(TAG, "FCM jetonu kaydedilemedi", it) }
    }

    suspend fun unregisterCurrentDevice(uid: String) {
        val token = runCatching {
            FirebaseMessaging.getInstance().token.await()
        }.getOrNull() ?: return
        runCatching { remote.remove(uid, token) }
            .onFailure { Log.e(TAG, "FCM jetonu silinemedi", it) }
    }

    suspend fun unregisterAll(uid: String) {
        runCatching { remote.removeAll(uid) }
            .onFailure { Log.e(TAG, "FCM jetonları silinemedi", it) }
    }

    private companion object {
        const val TAG = "Renown"
    }
}
