package com.mehmetbozkurt.questlog.feature.logedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.navigation.LogEditRouteKey
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.QuestLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class LogEditViewModel @Inject constructor(
    private val repository: QuestLogRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
): MviViewModel<LogEditState, LogEditEvent, LogEditEffect>(LogEditState()) {
    private val route = savedStateHandle.toRoute<LogEditRouteKey>()
    private var originalCreatedAt: Instant? = null
    private var originalCompleted: Boolean = false

    init {
        val existingId = route.logId
        if (existingId != null) {
            viewModelScope.launch {
                val log = repository.observeById(existingId).first()
                if (log != null) {
                    originalCreatedAt = log.createdAt
                    originalCompleted = log.isCompleted
                    setState {
                        copy(
                            id = log.id,
                            type = log.type,
                            title = log.title,
                            description = log.description,
                            priority = log.priority ?: Priority.MEDIUM,
                            dueAt = log.dueAt,
                            remindAt = log.remindAt,
                        )
                    }
                }
            }
        }
    }

    override fun onEvent(event: LogEditEvent) {
        when (event) {
            is LogEditEvent.TypeChanged -> setState { copy(type = event.value) }
            is LogEditEvent.TitleChanged -> setState { copy(title = event.value) }
            is LogEditEvent.DescriptionChanged -> setState { copy(description = event.value) }
            is LogEditEvent.PriorityChanged -> setState { copy(priority = event.value) }
            is LogEditEvent.DueAtChanged -> setState { copy(dueAt = event.value, showDuePicker = false) }
            is LogEditEvent.RemindAtChanged -> setState { copy(remindAt = event.value, showRemindPicker = false) }
            is LogEditEvent.DuePickerToggled -> setState { copy(showDuePicker = event.show) }
            is LogEditEvent.RemindPickerToggled -> setState { copy(showRemindPicker = event.show) }
            LogEditEvent.SaveClicked -> save()
        }
    }

    private fun save() {
        val state = currentState
        if (!state.canSave) return

        setState { copy(isSaving = true) }

        viewModelScope.launch {
            val user = authRepository.currentUserSync()
            if (user == null) {
                setState { copy(isSaving = false) }
                sendEffect(LogEditEffect.ShowError("Oturum bulunamadı"))
                return@launch
            }

            val now = Instant.now()
            val isQuest = state.type == LogType.QUEST

            val log = QuestLog(
                id = state.id ?: repository.newId(),
                ownerId = user.uid,
                campaignId = null,
                type = state.type,
                title = state.title.trim(),
                description = state.description.trim(),
                categoryId = null,
                priority = if (isQuest) state.priority else null,
                dueAt = if (isQuest) state.dueAt else null,
                remindAt = if (isQuest) state.remindAt else null,
                isCompleted = originalCompleted,
                createdAt = originalCreatedAt ?: now,
                updatedAt = now,
            )

            repository.upsert(log)
            sendEffect(LogEditEffect.NavigateBack)
        }
    }
}




















