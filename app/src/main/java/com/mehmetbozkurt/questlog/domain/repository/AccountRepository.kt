package com.mehmetbozkurt.questlog.domain.repository

interface AccountRepository {
    fun isPasswordAccount(): Boolean
    suspend fun deleteAccount(password: String): DeleteAccountResult
    suspend fun deleteAccountWithGoogle(idToken: String): DeleteAccountResult
}

sealed interface DeleteAccountResult {
    data object Success : DeleteAccountResult
    data object WrongPassword : DeleteAccountResult
    data object NoSession : DeleteAccountResult
    data class Failed(val message: String) : DeleteAccountResult
}
