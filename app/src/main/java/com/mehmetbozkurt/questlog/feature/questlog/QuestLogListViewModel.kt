package com.mehmetbozkurt.questlog.feature.questlog

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.domain.repository.QuestLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestLogListViewModel @Inject constructor(
    private val repository: QuestLogRepository
): MviViewModel<QuestLogListState, QuestLogListEvent, QuestLogListEffect> (
    QuestLogListState()
) {
    init {
        repository.observeAll().onEach { logs -> setState { copy(allLogs = logs, isLoading = false) } }
            .launchIn(viewModelScope)

    }

    override fun onEvent(event: QuestLogListEvent) {
        when (event) {
            is QuestLogListEvent.LogClicked ->
                sendEffect(QuestLogListEffect.NavigateToDetail(event.id))

            QuestLogListEvent.CreateClicked ->
                sendEffect(QuestLogListEffect.NavigateToCreate)

            is QuestLogListEvent.CompletionToggled -> viewModelScope.launch {
                val award = repository.setCompleted(event.id, event.completed)
                android.util.Log.d("QuestLog", "XP: $award")
            }

            is QuestLogListEvent.SearchChanged ->
                setState { copy(searchQuery = event.value) }

            is QuestLogListEvent.CompletionFilterChanged ->
                setState { copy(completionFilter = event.value) }

            is QuestLogListEvent.TypeFilterChanged ->
                setState { copy(typeFilter = event.value) }

            is QuestLogListEvent.PriorityFilterChanged ->
                setState { copy(priorityFilter = event.value) }

            is QuestLogListEvent.SortChanged ->
                setState { copy(sortOption = event.value) }

            is QuestLogListEvent.FilterSheetToggled ->
                setState { copy(showFilterSheet = event.show) }

            QuestLogListEvent.FiltersCleared -> setState {
                copy(
                    completionFilter = CompletionFilter.ALL,
                    typeFilter = null,
                    priorityFilter = null,
                )
            }
        }
    }
}