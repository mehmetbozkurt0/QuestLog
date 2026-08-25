package com.mehmetbozkurt.questlog.feature.crew

import com.mehmetbozkurt.questlog.core.common.UiText
import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.Crew
import com.mehmetbozkurt.questlog.domain.model.CrewFeedItem
import com.mehmetbozkurt.questlog.domain.model.CrewMember
import com.mehmetbozkurt.questlog.domain.model.CrewMessage
import com.mehmetbozkurt.questlog.domain.progression.CrewRules

enum class CrewTab { FEED, CHAT }

data class CrewState(
    val crew: Crew? = null,
    val members: List<CrewMember> = emptyList(),
    val feed: List<CrewFeedItem> = emptyList(),
    val messages: List<CrewMessage> = emptyList(),
    val tab: CrewTab = CrewTab.FEED,
    val messageInput: String = "",
    val unreadMessages: Int = 0,
    val ownUserId: String = "",
    val hasMentorFeat: Boolean = false,
    val approvalsToday: Int = 0,
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val showCreateDialog: Boolean = false,
    val showJoinDialog: Boolean = false,
    val showLeaveDialog: Boolean = false,
    val crewNameInput: String = "",
    val joinCodeInput: String = "",
) : UiState {
    val inCrew: Boolean get() = crew != null

    val canCreate: Boolean
        get() = !isWorking && crewNameInput.trim().length >= 3

    val canJoin: Boolean
        get() = !isWorking && joinCodeInput.trim().length == CrewRules.INVITE_CODE_LENGTH

    val canSendMessage: Boolean
        get() = messageInput.isNotBlank()

    val approvalsLeft: Int
        get() = (CrewRules.DAILY_APPROVAL_LIMIT - approvalsToday).coerceAtLeast(0)

    fun canApprove(item: CrewFeedItem): Boolean =
        hasMentorFeat &&
                item.authorId != ownUserId &&
                ownUserId !in item.approvedBy &&
                approvalsLeft > 0
}

sealed interface CrewEvent : UiEvent {
    data class CreateDialogToggled(val show: Boolean) : CrewEvent
    data class JoinDialogToggled(val show: Boolean) : CrewEvent
    data class LeaveDialogToggled(val show: Boolean) : CrewEvent
    data class CrewNameChanged(val value: String) : CrewEvent
    data class JoinCodeChanged(val value: String) : CrewEvent
    data object CreateConfirmed : CrewEvent
    data object JoinConfirmed : CrewEvent
    data object LeaveConfirmed : CrewEvent
    data class ApproveClicked(val entryId: String) : CrewEvent
    data object InviteCodeCopied : CrewEvent
    data class TabSelected(val tab: CrewTab) : CrewEvent
    data class MessageInputChanged(val value: String) : CrewEvent
    data object MessageSent : CrewEvent
    data class ChatVisibilityChanged(val visible: Boolean) : CrewEvent
}

sealed interface CrewEffect : UiEffect {
    data class ShowMessage(val text: UiText) : CrewEffect
    data class CopyToClipboard(val text: String) : CrewEffect
}
