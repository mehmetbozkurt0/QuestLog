package com.mehmetbozkurt.questlog.feature.profile

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.settings.SettingsRepository
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.repository.AccountRepository
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.DeleteAccountResult
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.CrewRepository
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
    characterRepository: CharacterRepository,
    crewRepository: CrewRepository,
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
) : MviViewModel<ProfileState, ProfileEvent, ProfileEffect>(ProfileState()) {
    init {
        authRepository.currentUser
            .onEach { user -> setState { copy(user = user) } }
            .launchIn(viewModelScope)

        settingsRepository.observeTheme()
            .onEach { theme -> setState { copy(theme = theme) } }
            .launchIn(viewModelScope)

        characterRepository.observeCharacter()
            .onEach { sheet -> setState { copy(character = sheet) } }
            .launchIn(viewModelScope)

        characterRepository.observeStreak()
            .onEach { info -> setState { copy(streak = info) } }
            .launchIn(viewModelScope)

        characterRepository.observeFeats()
            .onEach { feats -> setState { copy(featCount = feats.size) } }
            .launchIn(viewModelScope)

        crewRepository.observeCrewState()
            .onEach { crew -> setState { copy(crewName = crew.crew?.name) } }
            .launchIn(viewModelScope)

        questLogRepository.observeAll()
            .onEach { logs ->
                setState {
                    copy(
                        totalLogs = logs.size,
                        completedQuests = logs.count {
                            it.type == LogType.QUEST && it.isCompleted
                        },
                        activeQuests = logs.count {
                            it.type == LogType.QUEST && !it.isCompleted
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

            ProfileEvent.NotificationSettingsClicked ->
                sendEffect(ProfileEffect.OpenNotificationSettings)

            is ProfileEvent.DeleteDialogToggled -> setState {
                copy(
                    showDeleteDialog = event.show,
                    deletePassword = "",
                    deleteError = null,
                )
            }

            is ProfileEvent.DeletePasswordChanged -> setState {
                copy(deletePassword = event.value, deleteError = null)
            }

            ProfileEvent.DeleteConfirmed -> deleteAccount()
        }
    }

    private fun deleteAccount() {
        if (!currentState.canDelete) return
        val password = currentState.deletePassword

        setState { copy(isDeleting = true, deleteError = null) }

        viewModelScope.launch {
            when (val result = accountRepository.deleteAccount(password)) {
                DeleteAccountResult.Success -> {
                    setState { copy(isDeleting = false, showDeleteDialog = false) }
                    sendEffect(ProfileEffect.NavigateToAuth)
                }

                DeleteAccountResult.WrongPassword -> setState {
                    copy(isDeleting = false, deleteError = "Parola yanlış.")
                }

                DeleteAccountResult.NoSession -> setState {
                    copy(isDeleting = false, deleteError = "Oturum bulunamadı.")
                }

                is DeleteAccountResult.Failed -> setState {
                    copy(isDeleting = false, deleteError = result.message)
                }
            }
        }
    }
}
