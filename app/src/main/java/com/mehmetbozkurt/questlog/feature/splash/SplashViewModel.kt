package com.mehmetbozkurt.questlog.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface StartDestination {
    data object Loading : StartDestination
    data object Auth : StartDestination
    data object Home : StartDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {
    private val _startDestination = MutableStateFlow<StartDestination>(StartDestination.Loading)
    val startDestination : StateFlow<StartDestination> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            _startDestination.value = if (authRepository.currentUserSync() != null) StartDestination.Home else StartDestination.Auth
        }
    }
}