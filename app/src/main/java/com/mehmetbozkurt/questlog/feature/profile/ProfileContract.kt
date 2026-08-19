package com.mehmetbozkurt.questlog.feature.profile

import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.core.settings.ThemePreference
import com.mehmetbozkurt.questlog.domain.model.AppUser

data class ProfileState(
    val user: AppUser? = null,
    val totalLogs: Int = 0,
    val completedQuests: Int = 0,
    val showSignOutDialog: Boolean = false,
    val theme: ThemePreference = ThemePreference.SYSTEM
): UiState

sealed interface ProfileEvent: UiEvent {
    data class SignOutDialogToggled(val show: Boolean): ProfileEvent
    data object SignOutConfirmed: ProfileEvent
    data class ThemeChanged(val value: ThemePreference): ProfileEvent
}

sealed interface ProfileEffect: UiEffect{
    data object NavigateToAuth: ProfileEffect
}