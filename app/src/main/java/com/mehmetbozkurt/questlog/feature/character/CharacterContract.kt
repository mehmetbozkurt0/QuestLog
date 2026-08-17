package com.mehmetbozkurt.questlog.feature.character

import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.AcquiredFeat
import com.mehmetbozkurt.questlog.domain.model.CharacterSheet
import com.mehmetbozkurt.questlog.domain.model.FeatCatalog
import com.mehmetbozkurt.questlog.domain.model.FeatId
import com.mehmetbozkurt.questlog.domain.model.StatType

data class CharacterState(
    val character: CharacterSheet? = null,
    val feats: List<AcquiredFeat> = emptyList(),
    val isLoading: Boolean = true,
    val showFeatDialog: Boolean = false,
    val selectedFeatId: FeatId? = null,
    val selectedStatForFeat: StatType? = null,
    val isSavingFeat: Boolean = false,
): UiState{
    val levelProgress: Float
        get() {
            val char = character ?: return 0f
            if (char.xpToNextLevel <= 0) return 1f
            return (char.xpIntoLevel.toFloat() / char.xpToNextLevel).coerceIn(0f, 1f)
        }

    val hasPendingFeat: Boolean
        get() = (character?.pendingFeatChoices ?: 0) > 0

    val ownedFeatIds: Set<FeatId>
        get() = feats.map { it.featId }.toSet()

    val canConfirmFeat: Boolean
        get() {
            if (isSavingFeat) return false
            val id = selectedFeatId?: return false
            val needStat = FeatCatalog.byId(id).requiresStatChoice
            return !needStat || selectedStatForFeat != null
        }
}

sealed interface CharacterEvent: UiEvent {
    data class FeatDialogToggled(val show: Boolean): CharacterEvent
    data class FeatSelected(val featId: FeatId): CharacterEvent
    data class StatForFeatSelected(val statType: StatType): CharacterEvent
    data object FeatConfirmed: CharacterEvent
}

sealed interface CharacterEffect: UiEffect {
    data class ShowMessage(val text: String): CharacterEffect
}