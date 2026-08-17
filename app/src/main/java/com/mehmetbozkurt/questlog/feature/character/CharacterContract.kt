package com.mehmetbozkurt.questlog.feature.character

import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.AcquiredFeat
import com.mehmetbozkurt.questlog.domain.model.CharacterSheet

data class CharacterState(
    val character: CharacterSheet? = null,
    val feats: List<AcquiredFeat> = emptyList(),
    val isLoading: Boolean = true
): UiState{
    val levelProgress: Float
        get() {
            val char = character ?: return 0f
            if (char.xpToNextLevel <= 0) return 1f
            return (char.xpIntoLevel.toFloat() / char.xpToNextLevel).coerceIn(0f, 1f)
        }
}

sealed interface CharacterEvent: UiEvent

sealed interface CharacterEffect: UiEffect