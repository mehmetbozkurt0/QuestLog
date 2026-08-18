package com.mehmetbozkurt.questlog.feature.logdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.common.toCelebration
import com.mehmetbozkurt.questlog.core.common.toUserMessage
import com.mehmetbozkurt.questlog.core.navigation.LogDetailRouteKey
import com.mehmetbozkurt.questlog.domain.repository.QuestLogRepository
import com.mehmetbozkurt.questlog.domain.repository.XpAward
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogDetailViewModel @Inject constructor(
    private val repository: QuestLogRepository,
    savedStateHandle: SavedStateHandle
): MviViewModel<LogDetailState, LogDetailEvent, LogDetailEffect>(LogDetailState()) {
    private val logId = savedStateHandle.toRoute<LogDetailRouteKey>().id

    init {
        repository.observeById(logId)
            .onEach { log -> setState { copy(log = log, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: LogDetailEvent) {
        when (event) {
            LogDetailEvent.EditClicked ->
                sendEffect(LogDetailEffect.NavigateToEdit(logId))

            LogDetailEvent.CompletionToggled -> {
                val current = currentState.log ?: return
                viewModelScope.launch {
                    when (val award = repository.setCompleted(logId, !current.isCompleted)) {
                        is XpAward.Granted ->
                            sendEffect(LogDetailEffect.ShowCelebration(award.toCelebration()))
                        is XpAward.Rejected -> award.toUserMessage()?.let {
                            sendEffect(LogDetailEffect.ShowXpMessage(it))
                        }
                        null -> Unit
                    }
                }
            }

            is LogDetailEvent.DeleteDialogToggled ->
                setState { copy(showDeleteDialog = event.show) }

            LogDetailEvent.DeleteConfirmed -> viewModelScope.launch {
                repository.delete(logId)
                sendEffect(LogDetailEffect.NavigateBack)
            }
        }
    }
}