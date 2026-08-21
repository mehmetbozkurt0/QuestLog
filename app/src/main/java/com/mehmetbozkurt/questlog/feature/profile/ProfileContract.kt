package com.mehmetbozkurt.questlog.feature.profile

import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
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
    val theme: ThemePreference = ThemePreference.SYSTEM,
) : UiState {
    val level: Int get() = character?.level ?: 1

    val totalXp: Int get() = character?.totalXp ?: 0

    val levelProgress: Float
        get() {
            val char = character ?: return 0f
            if (char.xpToNextLevel <= 0) return 1f
            return (char.xpIntoLevel.toFloat() / char.xpToNextLevel).coerceIn(0f, 1f)
        }

    val isMaxLevel: Boolean get() = level >= XpCurve.MAX_LEVEL

    val title: String
        get() = when {
            level >= 16 -> "Efsanevi Maceracı"
            level >= 12 -> "Usta Maceracı"
            level >= 8 -> "Kıdemli Maceracı"
            level >= 4 -> "Gezgin"
            else -> "Çaylak"
        }
}

sealed interface ProfileEvent : UiEvent {
    data class SignOutDialogToggled(val show: Boolean) : ProfileEvent
    data object SignOutConfirmed : ProfileEvent
    data class ThemeChanged(val value: ThemePreference) : ProfileEvent
    data object NotificationSettingsClicked : ProfileEvent
}

sealed interface ProfileEffect : UiEffect {
    data object NavigateToAuth : ProfileEffect
    data object OpenNotificationSettings : ProfileEffect
}
