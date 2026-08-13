package com.mehmetbozkurt.questlog.feature.profile

import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.AppUser

data class ProfileState(
    val user: AppUser? = null,
    val totalLogs: Int = 0,
    val completedQuests: Int = 0,
    val showSignOutDialog: Boolean = false
): UiState

sealed interface ProfileEvent: UiEvent {
    data class SignOutDialogToggled(val show: Boolean): ProfileEvent
    data object SignOutConfirmed: ProfileEvent
}

sealed interface ProfileEffect: UiEffect{
    data object NavigateToAuth: ProfileEffect
}