package com.mehmetbozkurt.questlog.domain.repository

import android.net.Uri
import java.io.File

interface UserProfileRepository {
    suspend fun updateDisplayName(name: String): ProfileUpdateResult
    suspend fun updateAvatarFromUri(source: Uri): ProfileUpdateResult
    suspend fun updateAvatarFromFile(file: File): ProfileUpdateResult
    suspend fun removeAvatar(): ProfileUpdateResult
}

sealed interface ProfileUpdateResult {
    data object Success : ProfileUpdateResult
    data object NoSession : ProfileUpdateResult
    data object NameTooShort : ProfileUpdateResult
    data object ImageUnreadable : ProfileUpdateResult
    data object Offline : ProfileUpdateResult
    data object Failed : ProfileUpdateResult
}

object ProfileRules {
    const val NAME_MIN_LENGTH = 2
    const val NAME_MAX_LENGTH = 24
}
