package com.mehmetbozkurt.questlog.feature.crew

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.UiText
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.common.toUiText
import com.mehmetbozkurt.questlog.core.common.uiText
import com.mehmetbozkurt.questlog.core.common.withMinimumDuration
import com.mehmetbozkurt.questlog.core.sync.SyncScheduler
import com.mehmetbozkurt.questlog.core.notification.ChatPresence
import com.mehmetbozkurt.questlog.core.notification.CrewMessageNotifier
import com.mehmetbozkurt.questlog.core.settings.SettingsRepository
import com.mehmetbozkurt.questlog.domain.model.CrewMessage
import com.mehmetbozkurt.questlog.domain.model.FeatId
import com.mehmetbozkurt.questlog.domain.progression.CrewRules
import com.mehmetbozkurt.questlog.domain.repository.ApproveResult
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.CrewActionResult
import com.mehmetbozkurt.questlog.domain.repository.CrewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrewViewModel @Inject constructor(
    private val crewRepository: CrewRepository,
    private val characterRepository: CharacterRepository,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val chatPresence: ChatPresence,
    private val crewMessageNotifier: CrewMessageNotifier,
    private val syncScheduler: SyncScheduler,
) : MviViewModel<CrewState, CrewEvent, CrewEffect>(CrewState()) {

    init {
        setState { copy(ownUserId = authRepository.currentUserSync()?.uid.orEmpty()) }

        crewRepository.observeCrewState()
            .onEach { state ->
                setState {
                    copy(
                        crew = state.crew,
                        members = state.members,
                        feed = state.feed,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)

        crewRepository.observeMessages()
            .onEach { messages ->
                setState { copy(messages = messages) }
                if (chatPresence.isChatVisible) markSeen(messages)
            }
            .launchIn(viewModelScope)

        crewRepository.observeUnreadMessageCount()
            .onEach { count -> setState { copy(unreadMessages = count) } }
            .launchIn(viewModelScope)

        characterRepository.observeFeats()
            .onEach { feats ->
                setState { copy(hasMentorFeat = feats.any { it.featId == FeatId.MENTOR }) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch { crewRepository.refreshMemberCard() }
    }

    private fun refresh() {
        if (currentState.isRefreshing) return
        setState { copy(isRefreshing = true) }
        viewModelScope.launch {
            withMinimumDuration {
                crewRepository.refreshMemberCard()
                syncScheduler.requestSync()
            }
            setState { copy(isRefreshing = false) }
        }
    }

    override fun onEvent(event: CrewEvent) {
        when (event) {
            CrewEvent.Refresh -> refresh()

            is CrewEvent.CreateDialogToggled -> setState {
                copy(showCreateDialog = event.show, crewNameInput = "")
            }

            is CrewEvent.JoinDialogToggled -> setState {
                copy(showJoinDialog = event.show, joinCodeInput = "")
            }

            is CrewEvent.LeaveDialogToggled -> setState {
                copy(showLeaveDialog = event.show)
            }

            is CrewEvent.CrewNameChanged -> setState {
                copy(crewNameInput = event.value)
            }

            is CrewEvent.JoinCodeChanged -> setState {
                copy(joinCodeInput = event.value.uppercase().take(CrewRules.INVITE_CODE_LENGTH))
            }

            CrewEvent.CreateConfirmed -> createCrew()
            CrewEvent.JoinConfirmed -> joinCrew()
            CrewEvent.LeaveConfirmed -> leaveCrew()
            is CrewEvent.ApproveClicked -> approve(event.entryId)

            is CrewEvent.MemberMenuRequested -> setState { copy(memberMenuFor = event.member) }

            is CrewEvent.KickRequested -> setState {
                copy(kickTarget = event.member, memberMenuFor = null)
            }

            CrewEvent.KickConfirmed -> {
                val target = currentState.kickTarget ?: return
                runCrewAction(
                    successRes = R.string.crew_kick_done,
                    clearState = { copy(kickTarget = null) },
                ) { crewRepository.kickMember(target.userId) }
            }

            is CrewEvent.TransferRequested -> setState {
                copy(transferTarget = event.member, memberMenuFor = null)
            }

            CrewEvent.TransferConfirmed -> {
                val target = currentState.transferTarget ?: return
                runCrewAction(
                    successRes = R.string.crew_transfer_done,
                    clearState = { copy(transferTarget = null) },
                ) { crewRepository.transferOwnership(target.userId) }
            }

            is CrewEvent.RenameDialogToggled -> setState {
                copy(
                    showRenameDialog = event.show,
                    renameInput = if (event.show) crew?.name.orEmpty() else "",
                )
            }

            is CrewEvent.RenameInputChanged -> setState { copy(renameInput = event.value) }

            CrewEvent.RenameConfirmed -> {
                if (!currentState.canRename) return
                val name = currentState.renameInput
                runCrewAction(
                    successRes = R.string.crew_rename_done,
                    clearState = { copy(showRenameDialog = false, renameInput = "") },
                ) { crewRepository.renameCrew(name) }
            }

            is CrewEvent.RegenerateDialogToggled -> setState {
                copy(showRegenerateDialog = event.show)
            }

            CrewEvent.RegenerateConfirmed -> runCrewAction(
                successRes = R.string.crew_regenerate_done,
                clearState = { copy(showRegenerateDialog = false) },
            ) { crewRepository.regenerateInviteCode() }

            CrewEvent.InviteCodeShared -> {
                val code = currentState.crew?.inviteCode ?: return
                val name = currentState.crew?.name.orEmpty()
                sendEffect(CrewEffect.ShareInvite(crewName = name, code = code))
            }

            is CrewEvent.TabSelected -> setState { copy(tab = event.tab) }

            is CrewEvent.MessageInputChanged -> setState {
                copy(messageInput = event.value.take(CrewRules.MESSAGE_MAX_LENGTH))
            }

            CrewEvent.MessageSent -> sendMessage()

            is CrewEvent.ChatVisibilityChanged -> {
                chatPresence.setVisible(event.visible)
                if (event.visible) markChatSeen()
            }

            CrewEvent.InviteCodeCopied -> {
                val code = currentState.crew?.inviteCode ?: return
                sendEffect(CrewEffect.CopyToClipboard(code))
                sendEffect(CrewEffect.ShowMessage(uiText(R.string.crew_invite_code_copied)))
            }
        }
    }

    private fun createCrew() {
        if (!currentState.canCreate) return
        val name = currentState.crewNameInput.trim()
        setState { copy(isWorking = true) }
        viewModelScope.launch {
            val result = crewRepository.createCrew(name)
            setState { copy(isWorking = false, showCreateDialog = result !is CrewActionResult.Success) }
            sendEffect(CrewEffect.ShowMessage(result.message(R.string.crew_created)))
        }
    }

    private fun joinCrew() {
        if (!currentState.canJoin) return
        val code = currentState.joinCodeInput.trim()
        setState { copy(isWorking = true) }
        viewModelScope.launch {
            val result = crewRepository.joinByCode(code)
            setState { copy(isWorking = false, showJoinDialog = result !is CrewActionResult.Success) }
            sendEffect(CrewEffect.ShowMessage(result.message(R.string.crew_joined)))
        }
    }

    private fun leaveCrew() {
        setState { copy(isWorking = true, showLeaveDialog = false) }
        viewModelScope.launch {
            val result = crewRepository.leaveCrew()
            setState { copy(isWorking = false) }
            sendEffect(CrewEffect.ShowMessage(result.message(R.string.crew_left)))
        }
    }

    private fun markChatSeen() {
        crewMessageNotifier.clear()
        markSeen(currentState.messages)
    }

    private fun markSeen(messages: List<CrewMessage>) {
        val newest = messages.maxOfOrNull { it.sentAt.toEpochMilli() } ?: return
        viewModelScope.launch { settingsRepository.setLastSeenCrewMessageMillis(newest) }
    }

    private fun sendMessage() {
        if (!currentState.canSendMessage) return
        val text = currentState.messageInput
        setState { copy(messageInput = "") }
        viewModelScope.launch {
            val result = crewRepository.sendMessage(text)
            if (result !is CrewActionResult.Success) {
                setState { copy(messageInput = text) }
                sendEffect(CrewEffect.ShowMessage(result.message(R.string.crew_message_sent)))
            }
        }
    }

    private fun approve(entryId: String) {
        setState { copy(isWorking = true) }
        viewModelScope.launch {
            val result = crewRepository.approve(entryId)
            setState { copy(isWorking = false) }
            if (result is ApproveResult.Granted) {
                setState { copy(approvalsToday = approvalsToday + 1) }
            }
            sendEffect(CrewEffect.ShowMessage(result.message()))
        }
    }

    private fun CrewActionResult.message(successRes: Int): UiText = when (this) {
        CrewActionResult.Success -> uiText(successRes)
        CrewActionResult.AlreadyInCrew -> uiText(R.string.crew_error_already_in_crew)
        CrewActionResult.CodeNotFound -> uiText(R.string.crew_error_code_not_found)
        CrewActionResult.NotInCrew -> uiText(R.string.crew_error_not_in_crew)
        CrewActionResult.NotOwner -> uiText(R.string.crew_error_not_owner)
        CrewActionResult.MemberNotFound -> uiText(R.string.crew_error_member_not_found)
        CrewActionResult.InvalidName -> uiText(
            R.string.crew_error_invalid_name,
            CrewRules.NAME_MIN_LENGTH,
        )
        CrewActionResult.Offline -> uiText(R.string.crew_error_offline)
        is CrewActionResult.Failed -> reason.toUiText()
    }

    private fun runCrewAction(
        successRes: Int,
        clearState: CrewState.() -> CrewState,
        block: suspend () -> CrewActionResult,
    ) {
        setState { copy(isWorking = true) }
        viewModelScope.launch {
            val result = block()
            setState { copy(isWorking = false).let(clearState) }
            sendEffect(CrewEffect.ShowMessage(result.message(successRes)))
        }
    }

    private fun ApproveResult.message(): UiText = when (this) {
        is ApproveResult.Granted ->
            if (leveledUp) uiText(R.string.crew_approve_granted_leveled, xp, newLevel)
            else uiText(R.string.crew_approve_granted, xp)

        ApproveResult.NoMentorFeat -> uiText(R.string.crew_approve_error_no_mentor_feat)
        ApproveResult.DailyLimitReached ->
            uiText(R.string.crew_approve_error_daily_limit, CrewRules.DAILY_APPROVAL_LIMIT)

        ApproveResult.AlreadyApproved -> uiText(R.string.crew_approve_error_already_approved)
        ApproveResult.OwnQuest -> uiText(R.string.crew_approve_error_own_quest)
        is ApproveResult.Failed -> reason.toUiText()
    }
}
