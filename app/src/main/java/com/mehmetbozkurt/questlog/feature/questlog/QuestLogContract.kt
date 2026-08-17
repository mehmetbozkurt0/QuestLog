package com.mehmetbozkurt.questlog.feature.questlog

import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import com.mehmetbozkurt.questlog.domain.model.StatType

enum class CompletionFilter { ALL, COMPLETED, ACTIVE }

enum class SortOption { RECENT, DUE_DATE, PRIORITY, ALPHABETICAL }

fun SortOption.label(): String = when (this) {
    SortOption.RECENT -> "Son eklenen"
    SortOption.DUE_DATE -> "Son teslim tarihi"
    SortOption.PRIORITY -> "Öncelik"
    SortOption.ALPHABETICAL -> "Alfabetik"
}

fun CompletionFilter.label(): String = when (this) {
    CompletionFilter.ALL -> "Tümü"
    CompletionFilter.COMPLETED -> "Tamamlananlar"
    CompletionFilter.ACTIVE -> "Tamamlanmayanlar"
}

data class QuestLogListState(
    val allLogs: List<QuestLog> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val completionFilter: CompletionFilter = CompletionFilter.ALL,
    val statFilter: StatType? = null,
    val priorityFilter: Priority? = null,
    val sortOption: SortOption = SortOption.RECENT,
    val showFilterSheet: Boolean = false,
) : UiState {

    val logs: List<QuestLog>
        get() = allLogs
            .filter { log ->
                val q = searchQuery.trim()
                q.isBlank() ||
                        log.title.contains(q, ignoreCase = true) ||
                        log.description.contains(q, ignoreCase = true)
            }
            .filter { log ->
                when (completionFilter) {
                    CompletionFilter.ALL -> true
                    CompletionFilter.COMPLETED -> log.isCompleted
                    CompletionFilter.ACTIVE -> !log.isCompleted
                }
            }
            .filter { log -> statFilter == null || log.statType == statFilter }
            .filter { log -> priorityFilter == null || log.priority == priorityFilter }
            .sortedWith(sortComparator())

    private fun sortComparator(): Comparator<QuestLog> = when (sortOption) {
        SortOption.RECENT ->
            compareByDescending { it.createdAt }

        SortOption.DUE_DATE ->
            compareBy(nullsLast()) { it.dueAt }

        SortOption.PRIORITY ->
            compareByDescending<QuestLog> { it.priority?.ordinal ?: -1 }
                .thenByDescending { it.createdAt }

        SortOption.ALPHABETICAL ->
            compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }
    }

    val activeFilterCount: Int
        get() = listOfNotNull(
            completionFilter.takeIf { it != CompletionFilter.ALL },
            statFilter,
            priorityFilter,
        ).size

    val isEmpty: Boolean get() = !isLoading && logs.isEmpty()
    val isEmptyBecauseOfFilters: Boolean get() = isEmpty && allLogs.isNotEmpty()
}

sealed interface QuestLogListEvent : UiEvent {
    data class LogClicked(val id: String) : QuestLogListEvent
    data class CompletionToggled(val id: String, val completed: Boolean) : QuestLogListEvent
    data object CreateClicked : QuestLogListEvent
    data class SearchChanged(val value: String) : QuestLogListEvent
    data class CompletionFilterChanged(val value: CompletionFilter) : QuestLogListEvent
    data class StatFilterChanged(val value: StatType?): QuestLogListEvent
    data class PriorityFilterChanged(val value: Priority?) : QuestLogListEvent
    data class SortChanged(val value: SortOption) : QuestLogListEvent
    data class FilterSheetToggled(val show: Boolean) : QuestLogListEvent
    data object FiltersCleared : QuestLogListEvent
}

sealed interface QuestLogListEffect : UiEffect {
    data class NavigateToDetail(val id: String) : QuestLogListEffect
    data object NavigateToCreate : QuestLogListEffect
}