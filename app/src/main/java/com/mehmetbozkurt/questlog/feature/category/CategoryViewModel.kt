package com.mehmetbozkurt.questlog.feature.category

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository,
) : MviViewModel<CategoryState, CategoryEvent, CategoryEffect>(CategoryState()) {

    init {
        repository.observeAll()
            .onEach { list -> setState { copy(categories = list, isLoading = false) } }
            .launchIn(viewModelScope)

        viewModelScope.launch { repository.ensureDefaults() }
    }

    override fun onEvent(event: CategoryEvent) {
        when (event) {
            is CategoryEvent.AddDialogToggled -> setState {
                copy(showAddDialog = event.show, newName = "", newColorHex = CATEGORY_COLORS.first())
            }

            is CategoryEvent.NameChanged -> setState { copy(newName = event.value) }

            is CategoryEvent.ColorChanged -> setState { copy(newColorHex = event.value) }

            CategoryEvent.AddConfirmed -> {
                val s = currentState
                if (!s.canAdd) return
                viewModelScope.launch {
                    repository.create(s.newName, s.newColorHex)
                    setState { copy(showAddDialog = false, newName = "") }
                }
            }

            is CategoryEvent.DeleteRequested -> setState { copy(pendingDelete = event.category) }

            CategoryEvent.DeleteConfirmed -> {
                val target = currentState.pendingDelete ?: return
                viewModelScope.launch {
                    repository.delete(target.id)
                    setState { copy(pendingDelete = null) }
                    sendEffect(CategoryEffect.ShowMessage("${target.name} silindi"))
                }
            }
        }
    }
}