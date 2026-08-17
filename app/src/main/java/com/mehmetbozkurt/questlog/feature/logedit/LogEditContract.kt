package com.mehmetbozkurt.questlog.feature.logedit

import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.Category
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.StatType
import java.time.Instant

data class LogEditState(
    val id: String? = null,
    val type: LogType = LogType.QUEST,
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val dueAt: Instant? = null,
    val remindAt: Instant? = null,
    val isSaving: Boolean = false,
    val showDuePicker: Boolean = false,
    val showRemindPicker: Boolean = false,
    val statType: StatType? = null,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val categories: List<Category> = emptyList(),
    val categoryId: String? = null
): UiState{
    val isEditMode: Boolean get() = id != null
    val canSave: Boolean get() = !isSaving && title.isNotEmpty()
    val showQuestFields: Boolean get() = type == LogType.QUEST
}

sealed interface LogEditEvent: UiEvent {
    data class TypeChanged(val value: LogType) : LogEditEvent
    data class TitleChanged(val value: String) : LogEditEvent
    data class DescriptionChanged(val value: String) : LogEditEvent
    data class PriorityChanged(val value: Priority) : LogEditEvent
    data class DueAtChanged(val value: Instant?) : LogEditEvent
    data class StatTypeChanged(val value: StatType) : LogEditEvent
    data class DifficultyChanged(val value: Difficulty) : LogEditEvent
    data class RemindAtChanged(val value: Instant?) : LogEditEvent
    data class DuePickerToggled(val show: Boolean) : LogEditEvent
    data class RemindPickerToggled(val show: Boolean) : LogEditEvent
    data object SaveClicked : LogEditEvent
    data class CategoryChanged(val id: String?): LogEditEvent
}

sealed interface LogEditEffect: UiEffect {
    data object NavigateBack: LogEditEffect
    data class  ShowError(val message: String): LogEditEffect
}