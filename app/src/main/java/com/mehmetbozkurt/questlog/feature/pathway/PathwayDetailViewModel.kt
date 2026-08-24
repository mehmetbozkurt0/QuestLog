package com.mehmetbozkurt.questlog.feature.pathway

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.common.toCelebration
import com.mehmetbozkurt.questlog.core.common.toUiText
import com.mehmetbozkurt.questlog.core.common.uiText
import com.mehmetbozkurt.questlog.core.navigation.PathwayDetailRouteKey
import com.mehmetbozkurt.questlog.domain.progression.PathwayRules
import com.mehmetbozkurt.questlog.domain.repository.PathwayRepository
import com.mehmetbozkurt.questlog.domain.repository.QuestCompletionResult
import com.mehmetbozkurt.questlog.domain.repository.StartResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PathwayDetailViewModel @Inject constructor(
    private val repository: PathwayRepository,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<PathwayDetailState, PathwayDetailEvent, PathwayDetailEffect>(
    PathwayDetailState()
) {

    private val pathwayId = savedStateHandle.toRoute<PathwayDetailRouteKey>().pathwayId

    init {
        repository.observeDetail(pathwayId)
            .onEach { detail -> setState { copy(detail = detail, isLoading = false) } }
            .launchIn(viewModelScope)

        repository.observeProgress()
            .onEach { list ->
                val active = list.count { it.isActive }
                setState {
                    copy(
                        activeCount = active,
                        canStartMore = active < PathwayRules.MAX_ACTIVE_PATHWAYS,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: PathwayDetailEvent) {
        when (event) {
            PathwayDetailEvent.StartClicked -> start()

            is PathwayDetailEvent.AbandonDialogToggled ->
                setState { copy(showAbandonDialog = event.show) }

            PathwayDetailEvent.AbandonConfirmed -> abandon()

            is PathwayDetailEvent.QuestClicked -> completeQuest(event.questId)
        }
    }

    private fun start() {
        if (currentState.isWorking) return
        setState { copy(isWorking = true) }

        viewModelScope.launch {
            val result = repository.startPathway(pathwayId)
            setState { copy(isWorking = false) }

            val message = when (result) {
                StartResult.Success -> uiText(R.string.pathway_start_success)
                StartResult.TooManyActive -> uiText(
                    R.string.pathway_start_too_many_active,
                    PathwayRules.MAX_ACTIVE_PATHWAYS,
                )

                StartResult.PrerequisiteMissing ->
                    uiText(R.string.pathway_start_prerequisite_missing)

                StartResult.AlreadyStarted -> uiText(R.string.pathway_start_already_started)
            }
            sendEffect(PathwayDetailEffect.ShowMessage(message))
        }
    }

    private fun abandon() {
        setState { copy(isWorking = true, showAbandonDialog = false) }

        viewModelScope.launch {
            repository.abandonPathway(pathwayId)
            setState { copy(isWorking = false) }
            sendEffect(PathwayDetailEffect.ShowMessage(uiText(R.string.pathway_abandoned)))
        }
    }

    private fun completeQuest(questId: String) {
        if (currentState.isWorking) return
        setState { copy(isWorking = true) }

        viewModelScope.launch {
            val result = repository.completeQuest(questId)
            setState { copy(isWorking = false) }
            when (result) {
                is QuestCompletionResult.Success ->
                    sendEffect(PathwayDetailEffect.ShowCelebration(result.toCelebration()))
                is QuestCompletionResult.Rejected ->
                    sendEffect(PathwayDetailEffect.ShowMessage(result.reason.toUiText()))
            }
        }
    }
}