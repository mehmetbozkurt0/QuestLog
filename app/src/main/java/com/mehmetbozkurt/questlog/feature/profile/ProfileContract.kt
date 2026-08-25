package com.mehmetbozkurt.questlog.feature.profile

import androidx.annotation.StringRes
import com.mehmetbozkurt.questlog.core.common.UiText
import com.mehmetbozkurt.questlog.core.common.levelRankRes
import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.core.settings.AppLanguage
import com.mehmetbozkurt.questlog.core.settings.AppPalette
import com.mehmetbozkurt.questlog.core.settings.ThemePreference
import com.mehmetbozkurt.questlog.domain.model.AppUser
import com.mehmetbozkurt.questlog.domain.model.CharacterSheet
import com.mehmetbozkurt.questlog.domain.progression.StreakInfo
import com.mehmetbozkurt.questlog.domain.progression.XpCurve

data class ProfileState(
    val user: AppUser? = null,
    val character: CharacterSheet? = null,
    val streak: StreakInfo? = null,
    val featCount: Int = 0,
    val crewName: String? = null,
    val totalLogs: Int = 0,
    val completedQuests: Int = 0,
    val activeQuests: Int = 0,
    val showSignOutDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deletePassword: String = "",
    val isDeleting: Boolean = false,
    val deleteError: UiText? = null,
    val isPasswordAccount: Boolean = true,
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val palette: AppPalette = AppPalette.Default,
    val language: AppLanguage = AppLanguage.SYSTEM,
) : UiState {
    val canDelete: Boolean
        get() = !isDeleting && (!isPasswordAccount || deletePassword.length >= 6)

    val level: Int get() = character?.level ?: 1

    val totalXp: Int get() = character?.totalXp ?: 0

    val levelProgress: Float
        get() {
            val char = character ?: return 0f
            if (char.xpToNextLevel <= 0) return 1f
            return (char.xpIntoLevel.toFloat() / char.xpToNextLevel).coerceIn(0f, 1f)
        }

    val isMaxLevel: Boolean get() = level >= XpCurve.MAX_LEVEL

    @get:StringRes
    val titleRes: Int
        get() = levelRankRes(level)
}

sealed interface ProfileEvent : UiEvent {
    data class SignOutDialogToggled(val show: Boolean) : ProfileEvent
    data object SignOutConfirmed : ProfileEvent
    data class ThemeChanged(val value: ThemePreference) : ProfileEvent
    data class PaletteChanged(val value: AppPalette) : ProfileEvent
    data class LanguageChanged(val value: AppLanguage) : ProfileEvent
    data object NotificationSettingsClicked : ProfileEvent
    data class DeleteDialogToggled(val show: Boolean) : ProfileEvent
    data class DeletePasswordChanged(val value: String) : ProfileEvent
    data object DeleteConfirmed : ProfileEvent
    data class GoogleReauthToken(val idToken: String) : ProfileEvent
    data class GoogleReauthFailed(val message: UiText?) : ProfileEvent
}

sealed interface ProfileEffect : UiEffect {
    data object NavigateToAuth : ProfileEffect
    data object OpenNotificationSettings : ProfileEffect
    data object LaunchGoogleReauth : ProfileEffect
}
