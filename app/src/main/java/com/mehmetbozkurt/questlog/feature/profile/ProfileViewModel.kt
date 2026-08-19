package com.mehmetbozkurt.questlog.feature.profile

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.settings.SettingsRepository
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.QuestLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    questLogRepository: QuestLogRepository,
    private val settingsRepository: SettingsRepository
): MviViewModel<ProfileState, ProfileEvent, ProfileEffect>(ProfileState()) {
    init {
        authRepository.currentUser
            .onEach { user -> setState { copy(user = user) } }
            .launchIn(viewModelScope)

        settingsRepository.observeTheme()
            .onEach { theme -> setState { copy(theme = theme) } }
            .launchIn(viewModelScope)

        questLogRepository.observeAll()
            .onEach { logs ->
                setState {
                    copy(
                        totalLogs = logs.size,
                        completedQuests = logs.count {
                            it.type == LogType.QUEST && it.isCompleted
                        },
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.SignOutDialogToggled ->
                setState { copy(showSignOutDialog = event.show) }

            ProfileEvent.SignOutConfirmed -> {
                setState { copy(showSignOutDialog = false) }
                authRepository.signOut()
                sendEffect(ProfileEffect.NavigateToAuth)
            }

            is ProfileEvent.ThemeChanged -> viewModelScope.launch {
                settingsRepository.setTheme(event.value)
            }
        }
    }
}