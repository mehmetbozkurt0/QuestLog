package com.mehmetbozkurt.questlog.core.common

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.FirebaseNetworkException

fun Throwable.toAuthMessage(): String = when (this) {
    is FirebaseAuthWeakPasswordException ->
        "Parola çok zayıf. En az 6 karakter kullan."
    is FirebaseAuthInvalidCredentialsException ->
        "E-posta veya parola hatalı."
    is FirebaseAuthUserCollisionException ->
        "Bu e-posta zaten kayıtlı."
    is FirebaseAuthInvalidUserException ->
        "Böyle bir hesap bulunamadı."
    is FirebaseNetworkException ->
        "Bağlantı kurulamadı. İnternetini kontrol et."
    else ->
        "Bir şeyler ters gitti. Tekrar dene."
}