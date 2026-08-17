package com.mehmetbozkurt.questlog.feature.character

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CharacterViewModel @Inject constructor(
    repository: CharacterRepository
): MviViewModel<CharacterState, CharacterEvent, CharacterEffect>(CharacterState()) {
    init {
        repository.observeCharacter().onEach { sheet ->
            setState { copy(character = sheet, isLoading = false) }
        }.launchIn(viewModelScope)

        repository.observeFeats().onEach { feats ->
            setState { copy(feats = feats) }
        }.launchIn(viewModelScope)
    }

    override fun onEvent(event: CharacterEvent) = Unit
}