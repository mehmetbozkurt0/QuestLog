package com.mehmetbozkurt.questlog.feature.catalog

import com.mehmetbozkurt.questlog.core.common.Celebration
import com.mehmetbozkurt.questlog.core.common.UiText
import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.CatalogEntry
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.progression.CatalogRules

data class CatalogState(
    val entries: List<CatalogEntry> = emptyList(),
    val doneToday: Int = 0,
    val statFilter: StatType? = null,
    val isLoading: Boolean = true,
) : UiState {

    val remainingToday: Int get() = (CatalogRules.MAX_PER_DAY - doneToday).coerceAtLeast(0)

    val visibleEntries: List<CatalogEntry>
        get() = entries.filter { statFilter == null || it.task.statType == statFilter }

    val isEmpty: Boolean get() = !isLoading && entries.isEmpty()
}

sealed interface CatalogEvent : UiEvent {
    data class TaskCompleted(val taskId: String) : CatalogEvent
    data class StatFilterChanged(val value: StatType?) : CatalogEvent
}

sealed interface CatalogEffect : UiEffect {
    data class ShowMessage(val text: UiText) : CatalogEffect
    data class ShowCelebration(val celebration: Celebration) : CatalogEffect
}
