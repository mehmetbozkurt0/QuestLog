package com.mehmetbozkurt.questlog.feature.auth

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.core.common.DataResult
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.toAuthMessage
import com.mehmetbozkurt.questlog.core.common.uiText
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
): MviViewModel<AuthState, AuthEvent, AuthEffect>(AuthState()) {
    override fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged ->
                setState { copy(email = event.value, errorMessage = null) }
            is AuthEvent.PasswordChanged ->
                setState { copy(password = event.value, errorMessage = null) }
            is AuthEvent.DisplayNameChanged ->
                setState {copy(displayName = event.value, errorMessage = null)}

            AuthEvent.ModeToggled -> setState {
                copy(mode = if (mode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN, errorMessage = null)
            }

            AuthEvent.SubmitClicked -> submit()

            AuthEvent.ErrorDismissed -> setState {
                copy(errorMessage = null)
            }

            AuthEvent.ForgotPasswordClicked -> sendPasswordReset()

            AuthEvent.GoogleSignInClicked -> {
                setState { copy(isGoogleLoading = true, errorMessage = null) }
                sendEffect(AuthEffect.LaunchGoogleSignIn)
            }

            is AuthEvent.GoogleIdTokenReceived -> signInWithGoogle(event.idToken)

            is AuthEvent.GoogleSignInFailed -> setState {
                copy(isGoogleLoading = false, errorMessage = event.message)
            }
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is DataResult.Error -> setState {
                    copy(isGoogleLoading = false, errorMessage = result.exception.toAuthMessage())
                }

                is DataResult.Success -> {
                    setState { copy(isGoogleLoading = false) }
                    sendEffect(
                        if (result.data.isNewUser) AuthEffect.NavigateToOnboarding
                        else AuthEffect.NavigateToHome
                    )
                }
            }
        }
    }

    private fun sendPasswordReset() {
        val state = currentState
        if (state.email.isBlank() || !state.isEmailValid) {
            setState { copy(errorMessage = uiText(R.string.auth_email_required)) }
            return
        }

        setState { copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = authRepository.sendPasswordReset(state.email)) {
                is DataResult.Error -> setState {
                    copy(isLoading = false, errorMessage = result.exception.toAuthMessage())
                }

                is DataResult.Success<*> -> {
                    setState { copy(isLoading = false) }
                    sendEffect(
                        AuthEffect.ShowMessage(
                            uiText(R.string.auth_reset_link_sent, state.email.trim())
                        )
                    )
                }
            }
        }
    }
    private fun submit() {
        val state = currentState
        if (!state.canSubmit) {
            return
        }

        setState { copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = when (state.mode) {
                AuthMode.SIGN_IN -> authRepository.signIn(state.email, state.password)
                AuthMode.SIGN_UP -> authRepository.signUp(state.email, state.password, state.displayName)
            }

            when (result) {
                is DataResult.Error -> setState { copy(isLoading = false, errorMessage = result.exception.toAuthMessage()) }
                is DataResult.Success<*> -> {
                    setState { copy(isLoading = false) }
                    sendEffect(
                        if (state.mode == AuthMode.SIGN_UP) AuthEffect.NavigateToOnboarding
                        else AuthEffect.NavigateToHome
                    )
                }
            }
        }
    }
}