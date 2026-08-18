package com.mehmetbozkurt.questlog.feature.pathway

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.navigation.PathwayDetailRouteKey
import com.mehmetbozkurt.questlog.domain.progression.PathwayRules
import com.mehmetbozkurt.questlog.domain.repository.PathwayRepository
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

            is PathwayDetailEvent.QuestClicked -> Unit
        }
    }

    private fun start() {
        if (currentState.isWorking) return
        setState { copy(isWorking = true) }

        viewModelScope.launch {
            val result = repository.startPathway(pathwayId)
            setState { copy(isWorking = false) }

            val message = when (result) {
                StartResult.Success -> "Yola girdin. Bol şans."
                StartResult.TooManyActive ->
                    "Aynı anda en fazla ${PathwayRules.MAX_ACTIVE_PATHWAYS} yol sürdürebilirsin."
                StartResult.PrerequisiteMissing -> "Önce gerekli yolu tamamlamalısın."
                StartResult.AlreadyStarted -> "Bu yolda zaten ilerliyorsun."
            }
            sendEffect(PathwayDetailEffect.ShowMessage(message))
        }
    }

    private fun abandon() {
        setState { copy(isWorking = true, showAbandonDialog = false) }

        viewModelScope.launch {
            repository.abandonPathway(pathwayId)
            setState { copy(isWorking = false) }
            sendEffect(PathwayDetailEffect.ShowMessage("Yolu bıraktın. Emanetteki XP kayboldu."))
        }
    }
}