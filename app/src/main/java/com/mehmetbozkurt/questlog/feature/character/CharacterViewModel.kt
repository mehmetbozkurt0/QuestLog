package com.mehmetbozkurt.questlog.feature.character

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.domain.model.FeatCatalog
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val repository: CharacterRepository,
) : MviViewModel<CharacterState, CharacterEvent, CharacterEffect>(CharacterState()) {

    init {
        repository.observeCharacter()
            .onEach { sheet ->
                setState { copy(character = sheet, isLoading = false) }
            }
            .launchIn(viewModelScope)

        repository.observeFeats()
            .onEach { feats -> setState { copy(feats = feats) } }
            .launchIn(viewModelScope)

        repository.observeStreak()
            .onEach { info -> setState { copy(streak = info) } }
            .launchIn(viewModelScope)

        repository.observeWeeklySummary()
            .onEach { summary -> setState { copy(weekly = summary) } }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: CharacterEvent) {
        when (event) {
            is CharacterEvent.FeatDialogToggled -> setState {
                copy(
                    showFeatDialog = event.show,
                    selectedFeatId = null,
                    selectedStatForFeat = null,
                )
            }

            is CharacterEvent.FeatSelected -> setState {
                copy(
                    selectedFeatId = event.featId,
                    selectedStatForFeat = null,
                )
            }

            is CharacterEvent.StatForFeatSelected -> setState {
                copy(selectedStatForFeat = event.statType)
            }

            CharacterEvent.FeatConfirmed -> confirmFeat()
        }
    }

    private fun confirmFeat() {
        val s = currentState
        if (!s.canConfirmFeat) return
        val featId = s.selectedFeatId ?: return

        setState { copy(isSavingFeat = true) }

        viewModelScope.launch {
            repository.chooseFeat(featId, s.selectedStatForFeat)
            setState {
                copy(
                    isSavingFeat = false,
                    showFeatDialog = false,
                    selectedFeatId = null,
                    selectedStatForFeat = null,
                )
            }
            sendEffect(
                CharacterEffect.ShowMessage(
                    "${FeatCatalog.byId(featId).name} kazanıldı!"
                )
            )
        }
    }
}