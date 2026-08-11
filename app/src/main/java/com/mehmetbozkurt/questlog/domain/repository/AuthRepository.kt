package com.mehmetbozkurt.questlog.domain.repository

import com.mehmetbozkurt.questlog.core.common.DataResult
import com.mehmetbozkurt.questlog.domain.model.AppUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AppUser?>
    fun currentUserSync(): AppUser?
    suspend fun signIn(email: String, password: String): DataResult<AppUser>
    suspend fun signUp(email: String, password: String, displayName: String): DataResult<AppUser>
    fun signOut()
}