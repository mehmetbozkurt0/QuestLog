package com.mehmetbozkurt.questlog.feature.pathway

import com.mehmetbozkurt.questlog.core.common.Celebration
import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.PathwayDetail

data class PathwayDetailState(
    val detail: PathwayDetail? = null,
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val showAbandonDialog: Boolean = false,
    val activeCount: Int = 0,
    val canStartMore: Boolean = true,
) : UiState {
    val isActive: Boolean get() = detail?.progress?.isActive == true
    val isCompleted: Boolean get() = detail?.progress?.isCompleted == true
}

sealed interface PathwayDetailEvent : UiEvent {
    data object StartClicked : PathwayDetailEvent
    data class AbandonDialogToggled(val show: Boolean) : PathwayDetailEvent
    data object AbandonConfirmed : PathwayDetailEvent
    data class QuestClicked(val questId: String) : PathwayDetailEvent
}

sealed interface PathwayDetailEffect : UiEffect {
    data class ShowMessage(val text: String) : PathwayDetailEffect
    data class ShowCelebration(val celebration: Celebration) : PathwayDetailEffect
}