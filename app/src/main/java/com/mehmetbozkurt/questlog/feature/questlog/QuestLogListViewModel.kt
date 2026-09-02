package com.mehmetbozkurt.questlog.feature.questlog

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.common.toCelebration
import com.mehmetbozkurt.questlog.core.common.toUserMessage
import com.mehmetbozkurt.questlog.core.common.withMinimumDuration
import com.mehmetbozkurt.questlog.core.sync.SyncScheduler
import com.mehmetbozkurt.questlog.domain.repository.CatalogRepository
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.PathwayRepository
import com.mehmetbozkurt.questlog.domain.repository.QuestLogRepository
import com.mehmetbozkurt.questlog.domain.repository.XpAward
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
    private val pathwayRepository: PathwayRepository,
    private val catalogRepository: CatalogRepository,
    private val syncScheduler: SyncScheduler,
): MviViewModel<QuestLogListState, QuestLogListEvent, QuestLogListEffect> (
    QuestLogListState()
) {
    init {
        repository.observeAll().onEach { logs -> setState { copy(allLogs = logs, isLoading = false) } }
            .launchIn(viewModelScope)

        repository.observeHabitSlots().onEach { slots ->
            setState { copy(habitSlots = slots) }
        }.launchIn(viewModelScope)

        characterRepository.observeCharacter().onEach { sheet ->
            setState { copy(character = sheet) }
        }.launchIn(viewModelScope)

        characterRepository.observeStreak().onEach { info ->
            setState { copy(streak = info) }
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


    private fun refresh() {
        if (currentState.isRefreshing) return
        setState { copy(isRefreshing = true) }
        viewModelScope.launch {
            withMinimumDuration {
                pathwayRepository.refreshCatalog()
                catalogRepository.refreshCatalog()
                syncScheduler.requestSync()
            }
            setState { copy(isRefreshing = false) }
        }
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

            QuestLogListEvent.Refresh -> refresh()

            QuestLogListEvent.CatalogClicked ->
                sendEffect(QuestLogListEffect.NavigateToCatalog)

            is QuestLogListEvent.HabitSlotClicked -> {
                val slot = currentState.habitSlots.getOrNull(event.index)
                val quest = slot?.quest
                if (quest == null) {
                    sendEffect(QuestLogListEffect.NavigateToCreate(event.index))
                } else {
                    sendEffect(QuestLogListEffect.NavigateToDetail(quest.id))
                }
            }

            is QuestLogListEvent.HabitClearRequested ->
                setState { copy(slotPendingClear = event.index) }

            QuestLogListEvent.HabitClearConfirmed -> {
                val index = currentState.slotPendingClear
                setState { copy(slotPendingClear = null) }
                if (index != null) {
                    viewModelScope.launch { repository.clearHabitSlot(index) }
                }
            }

            is QuestLogListEvent.CompletionToggled -> {
                val log = currentState.allLogs.firstOrNull { it.id == event.id }
                if (event.completed && log != null && log.isXpEligible) {
                    setState {
                        copy(proofSheetLogId = event.id, proofSheetTitle = log.title)
                    }
                } else {
                    viewModelScope.launch { complete(repository.setCompleted(event.id, event.completed)) }
                }
            }

            QuestLogListEvent.ProofSheetDismissed ->
                setState { copy(proofSheetLogId = null, proofSheetTitle = "") }

            is QuestLogListEvent.ProofConfirmed -> {
                setState { copy(proofSheetLogId = null, proofSheetTitle = "") }
                viewModelScope.launch {
                    complete(
                        repository.completeWithProof(event.id, event.note, event.photoLocalPath)
                    )
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

    private fun complete(award: XpAward?) {
        when (award) {
            is XpAward.Granted ->
                sendEffect(QuestLogListEffect.ShowCelebration(award.toCelebration()))

            is XpAward.Rejected -> award.toUserMessage()?.let {
                sendEffect(QuestLogListEffect.ShowXpMessage(it))
            }

            null -> Unit
        }
    }
}