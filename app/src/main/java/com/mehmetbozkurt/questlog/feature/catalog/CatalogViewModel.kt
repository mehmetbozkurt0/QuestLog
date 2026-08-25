package com.mehmetbozkurt.questlog.feature.catalog

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.common.toCelebration
import com.mehmetbozkurt.questlog.core.common.toUiText
import com.mehmetbozkurt.questlog.domain.repository.CatalogCompletionResult
import com.mehmetbozkurt.questlog.domain.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val repository: CatalogRepository,
) : MviViewModel<CatalogState, CatalogEvent, CatalogEffect>(CatalogState()) {

    init {
        repository.observeCatalog()
            .onEach { entries ->
                setState {
                    copy(
                        entries = entries,
                        doneToday = entries.count { it.doneToday },
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch { repository.refreshCatalog() }
    }

    override fun onEvent(event: CatalogEvent) {
        when (event) {
            is CatalogEvent.StatFilterChanged -> setState { copy(statFilter = event.value) }

            is CatalogEvent.TaskCompleted -> viewModelScope.launch {
                when (val result = repository.completeTask(event.taskId)) {
                    is CatalogCompletionResult.Success ->
                        sendEffect(CatalogEffect.ShowCelebration(result.award.toCelebration()))

                    is CatalogCompletionResult.Rejected ->
                        sendEffect(CatalogEffect.ShowMessage(result.reason.toUiText()))
                }
            }
        }
    }
}
