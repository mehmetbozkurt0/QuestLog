package com.mehmetbozkurt.questlog.core.common

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.FirebaseNetworkException
import com.mehmetbozkurt.questlog.R

fun Throwable.toAuthMessage(): UiText = uiText(
    when (this) {
        is FirebaseAuthWeakPasswordException -> R.string.error_weak_password
        is FirebaseAuthInvalidCredentialsException -> R.string.error_invalid_credentials
        is FirebaseAuthUserCollisionException -> R.string.error_email_in_use
        is FirebaseAuthInvalidUserException -> R.string.error_user_not_found
        is FirebaseNetworkException -> R.string.error_network
        else -> R.string.error_generic
    }
)
