package com.mehmetbozkurt.questlog.feature.logdetail

import com.mehmetbozkurt.questlog.core.common.Celebration
import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.QuestLog

data class LogDetailState(
    val log: QuestLog? = null,
    val isLoading: Boolean = true,
    val showDeleteDialog: Boolean = false,
    val showProofSheet: Boolean = false,
): UiState

sealed interface LogDetailEvent: UiEvent {
    data object EditClicked : LogDetailEvent
    data object CompletionToggled : LogDetailEvent
    data object ProofSheetDismissed : LogDetailEvent
    data class ProofConfirmed(
        val note: String?,
        val photoLocalPath: String?,
    ) : LogDetailEvent
    data class DeleteDialogToggled(val show: Boolean) : LogDetailEvent
    data object DeleteConfirmed : LogDetailEvent
}

sealed interface LogDetailEffect: UiEffect{
    data class NavigateToEdit(val id: String): LogDetailEffect
    data object NavigateBack: LogDetailEffect
    data class ShowXpMessage(val text: String): LogDetailEffect
    data class ShowCelebration(val celebration: Celebration): LogDetailEffect
}