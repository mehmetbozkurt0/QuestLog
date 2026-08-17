package com.mehmetbozkurt.questlog.feature.catalog

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.domain.model.CatalogQuest
import com.mehmetbozkurt.questlog.domain.repository.CatalogRepository
import com.mehmetbozkurt.questlog.domain.repository.QuestLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    questLogRepository: QuestLogRepository,
) : MviViewModel<CatalogState, CatalogEvent, CatalogEffect>(CatalogState()) {

    init {
        catalogRepository.observeCatalog()
            .onEach { quests ->
                setState { copy(allQuests = quests, isLoading = false) }
            }
            .launchIn(viewModelScope)

        questLogRepository.observeAll()
            .onEach { logs ->
                setState { copy(addedTitles = logs.map { it.title.lowercase() }.toSet()) }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: CatalogEvent) {
        when (event) {
            is CatalogEvent.StatSelected -> setState { copy(selectedStat = event.statType) }
            is CatalogEvent.QuestClicked -> addQuest(event.quest)
        }
    }

    private fun addQuest(quest: CatalogQuest) {
        if (currentState.isAlreadyAdded(quest)) {
            sendEffect(CatalogEffect.ShowMessage("Bu görev zaten listende"))
            return
        }
        if (currentState.addingId != null) return

        setState { copy(addingId = quest.id) }

        viewModelScope.launch {
            catalogRepository.addToMyQuests(quest)
            setState { copy(addingId = null) }
            sendEffect(CatalogEffect.ShowMessage("${quest.title} eklendi"))
        }
    }
}