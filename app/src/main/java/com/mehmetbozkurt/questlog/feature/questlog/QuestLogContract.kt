package com.mehmetbozkurt.questlog.feature.questlog

import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.QuestLog

data class QuestLogListState(
    val logs: List<QuestLog> = emptyList(),
    val isLoading: Boolean = true
): UiState {
    val isEmpty: Boolean get() = !isLoading && logs.isEmpty()
}

sealed interface QuestLogListEvent: UiEvent {
    data class LogClicked(val id: String) : QuestLogListEvent
    data class CompletionToggled(val id: String, val completed: Boolean): QuestLogListEvent
    data object CreateClicked : QuestLogListEvent
}

sealed interface QuestLogListEffect: UiEffect {
    data class NavigateToDetail(val id: String) : QuestLogListEffect
    data object NavigateToCreate : QuestLogListEffect
}