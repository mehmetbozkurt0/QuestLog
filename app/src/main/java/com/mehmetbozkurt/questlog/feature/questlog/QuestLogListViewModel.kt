package com.mehmetbozkurt.questlog.feature.questlog

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.common.toUserMessage
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.PathwayRepository
import com.mehmetbozkurt.questlog.domain.repository.QuestLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestLogListViewModel @Inject constructor(
    private val repository: QuestLogRepository,
    characterRepository: CharacterRepository,
    private val pathwayRepository: PathwayRepository
): MviViewModel<QuestLogListState, QuestLogListEvent, QuestLogListEffect> (
    QuestLogListState()
) {
    init {
        repository.observeAll().onEach { logs -> setState { copy(allLogs = logs, isLoading = false) } }
            .launchIn(viewModelScope)

        characterRepository.observeCharacter().onEach { sheet ->
            setState { copy(character = sheet) }
        }.launchIn(viewModelScope)

        combine(
            pathwayRepository.observePathways(),
            pathwayRepository.observeProgress()
        ){ pathways, progressList ->
            progressList.filter { it.isActive }.mapNotNull { progress ->
                val pathway = pathways.firstOrNull {it.id == progress.pathwayId}?: return@mapNotNull null
                ActivePathwaySummary(
                    pathway = pathway,
                    progress = progress,
                    completedQuests = 0,
                    totalQuests = 0,
                )
            }
        }.onEach { summaries -> loadPathwayCounts(summaries) }.launchIn(viewModelScope)
    }

    private fun loadPathwayCounts(summaries: List<ActivePathwaySummary>) {
        if (summaries.isEmpty()) {
            setState { copy(activePathways = emptyList()) }
            return
        }

        viewModelScope.launch {
            val enriched = summaries.map { summary ->
                val detail = pathwayRepository.detailSnapshot(summary.pathway.id)
                summary.copy(
                    completedQuests = detail?.completedQuests ?: 0,
                    totalQuests = detail?.totalQuests ?: 0,
                )
            }
            setState { copy(activePathways = enriched) }
        }
    }

    override fun onEvent(event: QuestLogListEvent) {
        when (event) {
            is QuestLogListEvent.LogClicked ->
                sendEffect(QuestLogListEffect.NavigateToDetail(event.id))

            QuestLogListEvent.CreateClicked ->
                sendEffect(QuestLogListEffect.NavigateToCreate)

            is QuestLogListEvent.CompletionToggled -> viewModelScope.launch {
                val award = repository.setCompleted(event.id, event.completed)
                award?.toUserMessage()?.let { msg ->
                    sendEffect(QuestLogListEffect.ShowXpMessage(msg))
                }
            }

            is QuestLogListEvent.SearchChanged ->
                setState { copy(searchQuery = event.value) }

            is QuestLogListEvent.CompletionFilterChanged ->
                setState { copy(completionFilter = event.value) }

            is QuestLogListEvent.StatFilterChanged ->
                setState { copy(statFilter = event.value) }

            is QuestLogListEvent.PriorityFilterChanged ->
                setState { copy(priorityFilter = event.value) }

            is QuestLogListEvent.SortChanged ->
                setState { copy(sortOption = event.value) }

            is QuestLogListEvent.FilterSheetToggled ->
                setState { copy(showFilterSheet = event.show) }

            QuestLogListEvent.FiltersCleared -> setState {
                copy(
                    completionFilter = CompletionFilter.ALL,
                    statFilter = null,
                    priorityFilter = null,
                )
            }

            QuestLogListEvent.PathwaysClicked ->
                sendEffect(QuestLogListEffect.NavigateToPathways)

            is QuestLogListEvent.PathwayClicked ->
                sendEffect(QuestLogListEffect.NavigateToPathwayDetail(event.pathwayId))

            QuestLogListEvent.CharacterClicked ->
                sendEffect(QuestLogListEffect.NavigateToCharacter)
        }
    }
}