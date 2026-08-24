package com.mehmetbozkurt.questlog.feature.questlog

import androidx.annotation.StringRes
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.Celebration
import com.mehmetbozkurt.questlog.core.common.UiText
import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.CharacterSheet
import com.mehmetbozkurt.questlog.domain.model.Pathway
import com.mehmetbozkurt.questlog.domain.model.PathwayProgress
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.progression.StreakInfo

enum class CompletionFilter { ALL, COMPLETED, ACTIVE }

enum class SortOption { RECENT, DUE_DATE, PRIORITY, ALPHABETICAL }

@StringRes
fun SortOption.labelRes(): Int = when (this) {
    SortOption.RECENT -> R.string.sort_recent
    SortOption.DUE_DATE -> R.string.sort_due_date
    SortOption.PRIORITY -> R.string.sort_priority
    SortOption.ALPHABETICAL -> R.string.sort_alphabetical
}

@StringRes
fun CompletionFilter.labelRes(): Int = when (this) {
    CompletionFilter.ALL -> R.string.completion_all
    CompletionFilter.COMPLETED -> R.string.completion_completed
    CompletionFilter.ACTIVE -> R.string.completion_active
}

data class ActivePathwaySummary(
    val pathway: Pathway,
    val progress: PathwayProgress,
    val completedQuests: Int,
    val totalQuests: Int,
) {
    val fraction: Float
        get() = if (totalQuests == 0) 0f
        else completedQuests.toFloat() / totalQuests
}

data class QuestLogListState(
    val allLogs: List<QuestLog> = emptyList(),
    val character: CharacterSheet? = null,
    val activePathways: List<ActivePathwaySummary> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val completionFilter: CompletionFilter = CompletionFilter.ALL,
    val statFilter: StatType? = null,
    val priorityFilter: Priority? = null,
    val sortOption: SortOption = SortOption.RECENT,
    val showFilterSheet: Boolean = false,
    val streak: StreakInfo? = null,
    val proofSheetLogId: String? = null,
    val proofSheetTitle: String = "",
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

    val isSearching: Boolean
        get() = searchQuery.isNotBlank() || activeFilterCount > 0

    val showHeaderSections: Boolean get() = !isSearching && !isLoading

    val levelProgress: Float
        get() {
            val c = character ?: return 0f
            if (c.xpToNextLevel <= 0) return 1f
            return (c.xpIntoLevel.toFloat() / c.xpToNextLevel).coerceIn(0f, 1f)
        }

    val activeLogs: List<QuestLog> get() = logs.filter { !it.isCompleted }
    val completedLogs: List<QuestLog> get() = logs.filter { it.isCompleted }

    val isEmpty: Boolean get() = !isLoading && logs.isEmpty()
    val isEmptyBecauseOfFilters: Boolean get() = isEmpty && allLogs.isNotEmpty()
    val isCompletelyEmpty: Boolean
        get() = !isLoading && allLogs.isEmpty() && activePathways.isEmpty()
}

sealed interface QuestLogListEvent : UiEvent {
    data class LogClicked(val id: String) : QuestLogListEvent
    data class CompletionToggled(val id: String, val completed: Boolean) : QuestLogListEvent
    data object ProofSheetDismissed : QuestLogListEvent
    data class ProofConfirmed(
        val id: String,
        val note: String?,
        val photoLocalPath: String?,
    ) : QuestLogListEvent
    data object CreateClicked : QuestLogListEvent
    data class SearchChanged(val value: String) : QuestLogListEvent
    data class CompletionFilterChanged(val value: CompletionFilter) : QuestLogListEvent
    data class StatFilterChanged(val value: StatType?) : QuestLogListEvent
    data class PriorityFilterChanged(val value: Priority?) : QuestLogListEvent
    data class SortChanged(val value: SortOption) : QuestLogListEvent
    data class FilterSheetToggled(val show: Boolean) : QuestLogListEvent
    data object FiltersCleared : QuestLogListEvent
    data object PathwaysClicked : QuestLogListEvent
    data class PathwayClicked(val pathwayId: String) : QuestLogListEvent
    data object CharacterClicked : QuestLogListEvent
}

sealed interface QuestLogListEffect : UiEffect {
    data class NavigateToDetail(val id: String) : QuestLogListEffect
    data object NavigateToCreate : QuestLogListEffect
    data class ShowXpMessage(val text: UiText) : QuestLogListEffect
    data object NavigateToPathways : QuestLogListEffect
    data class NavigateToPathwayDetail(val pathwayId: String) : QuestLogListEffect
    data object NavigateToCharacter : QuestLogListEffect
    data class ShowCelebration(val celebration: Celebration): QuestLogListEffect
}