package com.mehmetbozkurt.questlog.feature.home

import androidx.lifecycle.ViewModel
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignOutViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {
    fun signOut() = authRepository.signOut()
}