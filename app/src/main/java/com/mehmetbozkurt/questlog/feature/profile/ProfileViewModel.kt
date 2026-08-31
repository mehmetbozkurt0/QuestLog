package com.mehmetbozkurt.questlog.feature.profile

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.common.toUiText
import com.mehmetbozkurt.questlog.core.common.uiText
import com.mehmetbozkurt.questlog.core.notification.DeviceTokenManager
import com.mehmetbozkurt.questlog.core.settings.AppLocaleManager
import com.mehmetbozkurt.questlog.core.settings.SettingsRepository
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.repository.AccountRepository
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.DeleteAccountResult
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.CrewRepository
import com.mehmetbozkurt.questlog.domain.repository.ProfileUpdateResult
import com.mehmetbozkurt.questlog.domain.repository.QuestLogRepository
import com.mehmetbozkurt.questlog.domain.repository.UserProfileRepository
import com.mehmetbozkurt.questlog.core.common.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    questLogRepository: QuestLogRepository,
    characterRepository: CharacterRepository,
    crewRepository: CrewRepository,
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val deviceTokenManager: DeviceTokenManager,
    private val userProfileRepository: UserProfileRepository,
) : MviViewModel<ProfileState, ProfileEvent, ProfileEffect>(ProfileState()) {
    init {
        authRepository.currentUser
            .onEach { user -> setState { copy(user = user) } }
            .launchIn(viewModelScope)

        settingsRepository.observeTheme()
            .onEach { theme -> setState { copy(theme = theme) } }
            .launchIn(viewModelScope)

        settingsRepository.observePalette()
            .onEach { palette -> setState { copy(palette = palette) } }
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

        setState {
            copy(
                isPasswordAccount = accountRepository.isPasswordAccount(),
                language = AppLocaleManager.current(),
            )
        }
    }

    override fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.EditSheetToggled -> setState {
                copy(
                    showEditSheet = event.show,
                    nameInput = if (event.show) user?.displayName.orEmpty() else "",
                )
            }

            is ProfileEvent.NameInputChanged -> setState { copy(nameInput = event.value) }

            ProfileEvent.NameSaveClicked -> {
                if (!currentState.canSaveName) return
                val name = currentState.nameInput
                runProfileUpdate(R.string.profile_edit_name_saved) {
                    userProfileRepository.updateDisplayName(name)
                }
            }

            is ProfileEvent.AvatarPicked -> runProfileUpdate(R.string.profile_edit_photo_saved) {
                userProfileRepository.updateAvatarFromUri(event.uri)
            }

            is ProfileEvent.AvatarCaptured -> runProfileUpdate(R.string.profile_edit_photo_saved) {
                userProfileRepository.updateAvatarFromFile(event.file)
            }

            ProfileEvent.AvatarRemoveClicked ->
                runProfileUpdate(R.string.profile_edit_photo_removed) {
                    userProfileRepository.removeAvatar()
                }

            is ProfileEvent.SignOutDialogToggled ->
                setState { copy(showSignOutDialog = event.show) }

            ProfileEvent.SignOutConfirmed -> {
                setState { copy(showSignOutDialog = false) }
                viewModelScope.launch {
                    val uid = authRepository.currentUserSync()?.uid
                    if (uid != null) {
                        withTimeoutOrNull(TOKEN_CLEANUP_TIMEOUT_MS) {
                            deviceTokenManager.unregisterCurrentDevice(uid)
                        }
                    }
                    authRepository.signOut()
                    sendEffect(ProfileEffect.NavigateToAuth)
                }
            }

            is ProfileEvent.ThemeChanged -> viewModelScope.launch {
                settingsRepository.setTheme(event.value)
            }

            is ProfileEvent.PaletteChanged -> viewModelScope.launch {
                settingsRepository.setPalette(event.value)
            }

            is ProfileEvent.LanguageChanged -> {
                setState { copy(language = event.value) }
                AppLocaleManager.apply(event.value)
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

            ProfileEvent.DeleteConfirmed -> {
                if (currentState.isPasswordAccount) {
                    deleteAccount()
                } else {
                    setState { copy(isDeleting = true, deleteError = null) }
                    sendEffect(ProfileEffect.LaunchGoogleReauth)
                }
            }

            is ProfileEvent.GoogleReauthToken -> deleteAccountWithGoogle(event.idToken)

            is ProfileEvent.GoogleReauthFailed -> setState {
                copy(isDeleting = false, deleteError = event.message)
            }
        }
    }

    private fun runProfileUpdate(
        successRes: Int,
        block: suspend () -> ProfileUpdateResult,
    ) {
        setState { copy(isSavingProfile = true) }
        viewModelScope.launch {
            val result = block()
            setState { copy(isSavingProfile = false) }
            if (result == ProfileUpdateResult.Success) {
                setState { copy(showEditSheet = false) }
            }
            sendEffect(ProfileEffect.ShowMessage(result.message(successRes)))
        }
    }

    private fun ProfileUpdateResult.message(successRes: Int): UiText = when (this) {
        ProfileUpdateResult.Success -> uiText(successRes)
        ProfileUpdateResult.NoSession -> uiText(R.string.account_error_no_session)
        ProfileUpdateResult.NameTooShort -> uiText(R.string.profile_edit_name_too_short)
        ProfileUpdateResult.ImageUnreadable -> uiText(R.string.profile_edit_photo_unreadable)
        ProfileUpdateResult.Offline -> uiText(R.string.profile_edit_offline)
        ProfileUpdateResult.Failed -> uiText(R.string.profile_edit_failed)
    }

    private companion object {
        const val TOKEN_CLEANUP_TIMEOUT_MS = 3000L
    }

    private fun deleteAccount() {
        if (!currentState.canDelete) return
        val password = currentState.deletePassword

        setState { copy(isDeleting = true, deleteError = null) }

        viewModelScope.launch {
            handleDeleteResult(accountRepository.deleteAccount(password))
        }
    }

    private fun deleteAccountWithGoogle(idToken: String) {
        viewModelScope.launch {
            handleDeleteResult(accountRepository.deleteAccountWithGoogle(idToken))
        }
    }

    private fun handleDeleteResult(result: DeleteAccountResult) {
        when (result) {
            DeleteAccountResult.Success -> {
                setState { copy(isDeleting = false, showDeleteDialog = false) }
                sendEffect(ProfileEffect.NavigateToAuth)
            }

            DeleteAccountResult.WrongPassword -> setState {
                copy(
                    isDeleting = false,
                    deleteError = uiText(
                        if (isPasswordAccount) R.string.account_error_wrong_password
                        else R.string.account_error_reauth_failed
                    ),
                )
            }

            DeleteAccountResult.NoSession -> setState {
                copy(
                    isDeleting = false,
                    deleteError = uiText(R.string.account_error_no_session),
                )
            }

            is DeleteAccountResult.Failed -> setState {
                copy(isDeleting = false, deleteError = result.reason.toUiText())
            }
        }
    }
}
