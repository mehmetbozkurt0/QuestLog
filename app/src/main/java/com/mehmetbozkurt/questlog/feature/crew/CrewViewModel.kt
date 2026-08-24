package com.mehmetbozkurt.questlog.feature.crew

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.UiText
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.common.toUiText
import com.mehmetbozkurt.questlog.core.common.uiText
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

        characterRepository.observeFeats()
            .onEach { feats ->
                setState { copy(hasMentorFeat = feats.any { it.featId == FeatId.MENTOR }) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch { crewRepository.refreshMemberCard() }
    }

    override fun onEvent(event: CrewEvent) {
        when (event) {
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
        CrewActionResult.Offline -> uiText(R.string.crew_error_offline)
        is CrewActionResult.Failed -> reason.toUiText()
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
