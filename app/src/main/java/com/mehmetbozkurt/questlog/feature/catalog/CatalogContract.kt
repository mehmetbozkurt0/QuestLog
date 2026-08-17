package com.mehmetbozkurt.questlog.feature.catalog

import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.CatalogQuest
import com.mehmetbozkurt.questlog.domain.model.StatType

data class CatalogState(
    val allQuests: List<CatalogQuest> = emptyList(),
    val selectedStat: StatType = StatType.STR,
    val addedTitles: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val addingId: String? = null,
) : UiState {

    val questsForSelectedStat: List<CatalogQuest>
        get() = allQuests.filter { it.statType == selectedStat }

    fun isAlreadyAdded(quest: CatalogQuest): Boolean =
        quest.title.lowercase() in addedTitles

    val isEmpty: Boolean get() = !isLoading && allQuests.isEmpty()
}

sealed interface CatalogEvent: UiEvent {
    data class StatSelected(val statType: StatType): CatalogEvent
    data class QuestClicked(val quest: CatalogQuest): CatalogEvent
}

sealed interface CatalogEffect : UiEffect {
    data class ShowMessage(val text: String) : CatalogEffect
}