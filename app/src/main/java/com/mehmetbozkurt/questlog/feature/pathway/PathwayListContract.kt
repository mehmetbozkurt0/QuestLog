package com.mehmetbozkurt.questlog.feature.pathway

import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.Pathway
import com.mehmetbozkurt.questlog.domain.model.PathwayProgress
import com.mehmetbozkurt.questlog.domain.progression.PathwayRules

data class PathwayListItem(
    val pathway: Pathway,
    val progress: PathwayProgress?,
    val isLocked: Boolean,
    val requiredPathwayTitle: String?,
) {
    val isActive: Boolean get() = progress?.isActive == true
    val isCompleted: Boolean get() = progress?.isCompleted == true
}

data class PathwayListState(
    val items: List<PathwayListItem> = emptyList(),
    val isLoading: Boolean = true,
) : UiState {
    val activeItems: List<PathwayListItem> get() = items.filter { it.isActive }
    val availableItems: List<PathwayListItem>
        get() = items.filter { !it.isActive && !it.isCompleted }
    val completedItems: List<PathwayListItem> get() = items.filter { it.isCompleted }

    val activeCount: Int get() = activeItems.size
    val canStartMore: Boolean get() = activeCount < PathwayRules.MAX_ACTIVE_PATHWAYS
}

sealed interface PathwayListEvent : UiEvent {
    data class PathwayClicked(val pathwayId: String) : PathwayListEvent
}

sealed interface PathwayListEffect : UiEffect {
    data class NavigateToDetail(val pathwayId: String) : PathwayListEffect
}