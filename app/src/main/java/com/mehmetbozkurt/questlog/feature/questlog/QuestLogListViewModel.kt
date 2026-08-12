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
        repository.observeAll().onEach { logs -> setState { copy(logs = logs, isLoading = false) } }
            .launchIn(viewModelScope)

        //geçici test verileri ---- silinecek
        viewModelScope.launch {
            val uid = "test-user"
            repository.upsert(
                com.mehmetbozkurt.questlog.domain.model.QuestLog(
                    id = repository.newId(),
                    ownerId = uid,
                    campaignId = null,
                    type = com.mehmetbozkurt.questlog.domain.model.LogType.QUEST,
                    title = "Barovia'ya giriş",
                    description = "Sisler dağıldığında köyün kapıları önündeydik.",
                    categoryId = null,
                    priority = com.mehmetbozkurt.questlog.domain.model.Priority.HIGH,
                    dueAt = java.time.Instant.now().plusSeconds(86400 * 3),
                    remindAt = null,
                    isCompleted = false,
                    createdAt = java.time.Instant.now(),
                    updatedAt = java.time.Instant.now(),
                )
            )
        }
    }

    override fun onEvent(event: QuestLogListEvent) {
        when(event) {
            is QuestLogListEvent.LogClicked -> sendEffect(QuestLogListEffect.NavigateToDetail(event.id))

            QuestLogListEvent.CreateClicked -> sendEffect(QuestLogListEffect.NavigateToCreate)

            is QuestLogListEvent.CompletionToggled -> viewModelScope.launch {
                repository.setCompleted(event.id, event.completed)
            }
        }
    }
}