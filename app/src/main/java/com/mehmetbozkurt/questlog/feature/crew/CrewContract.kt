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
    val memberMenuFor: CrewMember? = null,
    val kickTarget: CrewMember? = null,
    val transferTarget: CrewMember? = null,
    val showRenameDialog: Boolean = false,
    val showRegenerateDialog: Boolean = false,
    val renameInput: String = "",
) : UiState {
    val inCrew: Boolean get() = crew != null

    val isOwner: Boolean get() = crew != null && crew.ownerId == ownUserId

    val canRename: Boolean
        get() = !isWorking &&
                renameInput.trim().length >= CrewRules.NAME_MIN_LENGTH &&
                renameInput.trim() != crew?.name

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
    data class MemberMenuRequested(val member: CrewMember?) : CrewEvent
    data class KickRequested(val member: CrewMember?) : CrewEvent
    data object KickConfirmed : CrewEvent
    data class TransferRequested(val member: CrewMember?) : CrewEvent
    data object TransferConfirmed : CrewEvent
    data class RenameDialogToggled(val show: Boolean) : CrewEvent
    data class RenameInputChanged(val value: String) : CrewEvent
    data object RenameConfirmed : CrewEvent
    data class RegenerateDialogToggled(val show: Boolean) : CrewEvent
    data object RegenerateConfirmed : CrewEvent
    data object InviteCodeShared : CrewEvent
    data class TabSelected(val tab: CrewTab) : CrewEvent
    data class MessageInputChanged(val value: String) : CrewEvent
    data object MessageSent : CrewEvent
    data class ChatVisibilityChanged(val visible: Boolean) : CrewEvent
}

sealed interface CrewEffect : UiEffect {
    data class ShowMessage(val text: UiText) : CrewEffect
    data class CopyToClipboard(val text: String) : CrewEffect
    data class ShareInvite(val crewName: String, val code: String) : CrewEffect
}
