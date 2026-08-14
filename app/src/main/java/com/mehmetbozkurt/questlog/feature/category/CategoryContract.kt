package com.mehmetbozkurt.questlog.feature.category

import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.Category

val CATEGORY_COLORS = listOf(
    "#C8A951", "#7B5EA7", "#5B8FA8", "#8C5A3C",
    "#6E8F6B", "#C1443A", "#A88C4A", "#4A6B8A",
)

data class CategoryState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val newName: String = "",
    val newColorHex: String = CATEGORY_COLORS.first(),
    val pendingDelete: Category? = null,
) : UiState {
    val canAdd: Boolean get() = newName.isNotBlank() &&
            categories.none { it.name.equals(newName.trim(), ignoreCase = true) }

    val isDuplicate: Boolean get() = newName.isNotBlank() &&
            categories.any { it.name.equals(newName.trim(), ignoreCase = true) }
}

sealed interface CategoryEvent : UiEvent {
    data class AddDialogToggled(val show: Boolean) : CategoryEvent
    data class NameChanged(val value: String) : CategoryEvent
    data class ColorChanged(val value: String) : CategoryEvent
    data object AddConfirmed : CategoryEvent
    data class DeleteRequested(val category: Category?) : CategoryEvent
    data object DeleteConfirmed : CategoryEvent
}

sealed interface CategoryEffect : UiEffect {
    data class ShowMessage(val text: String) : CategoryEffect
}