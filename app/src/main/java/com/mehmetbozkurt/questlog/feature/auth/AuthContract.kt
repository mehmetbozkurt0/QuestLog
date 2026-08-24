package com.mehmetbozkurt.questlog.feature.auth

import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState

enum class AuthMode { SIGN_IN, SIGN_UP}

data class AuthState (
    val mode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val errorMessage: String? = null
): UiState {
    val isEmailValid: Boolean
        get() = email.isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    val isBusy: Boolean
        get() = isLoading || isGoogleLoading

    val canSubmit: Boolean
        get() = !isBusy && email.isNotEmpty() && isEmailValid && password.length >= 6 && (mode == AuthMode.SIGN_IN || displayName.isNotEmpty())
}

sealed interface AuthEvent: UiEvent {
    data class EmailChanged(val value: String): AuthEvent
    data class PasswordChanged(val value: String): AuthEvent
    data class DisplayNameChanged(val value: String): AuthEvent

    data object ModeToggled : AuthEvent
    data object SubmitClicked : AuthEvent
    data object ErrorDismissed : AuthEvent
    data object ForgotPasswordClicked : AuthEvent
    data object GoogleSignInClicked : AuthEvent
    data class GoogleIdTokenReceived(val idToken: String) : AuthEvent
    data class GoogleSignInFailed(val message: String?) : AuthEvent
}

sealed interface AuthEffect: UiEffect {
    data object  NavigateToHome : AuthEffect
    data object NavigateToOnboarding : AuthEffect
    data class ShowMessage(val text: String) : AuthEffect
    data object LaunchGoogleSignIn : AuthEffect
}


























