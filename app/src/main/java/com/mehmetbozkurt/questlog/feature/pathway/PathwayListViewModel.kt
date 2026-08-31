package com.mehmetbozkurt.questlog.feature.pathway

import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.domain.repository.PathwayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PathwayListViewModel @Inject constructor(
    private val repository: PathwayRepository,
) : MviViewModel<PathwayListState, PathwayListEvent, PathwayListEffect>(PathwayListState()) {

    init {
        combine(
            repository.observePathways(),
            repository.observeProgress(),
            repository.observeQuestCounts(),
        ) { pathways, progressList, questCounts ->
            val progressMap = progressList.associateBy { it.pathwayId }
            val titleMap = pathways.associate { it.id to it.title }

            pathways.map { pathway ->
                val required = pathway.requiredPathwayId
                val locked = required != null &&
                        progressMap[required]?.isCompleted != true
                val progress = progressMap[pathway.id]
                val started = progress != null
                val detail = if (started) repository.detailSnapshot(pathway.id) else null

                PathwayListItem(
                    pathway = pathway,
                    progress = progress,
                    isLocked = locked,
                    requiredPathwayTitle = required?.let { titleMap[it] },
                    completedQuests = detail?.completedQuests ?: 0,
                    totalQuests = detail?.totalQuests ?: questCounts[pathway.id] ?: 0,
                )
            }
        }
            .onEach { items -> setState { copy(items = items, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: PathwayListEvent) {
        when (event) {
            is PathwayListEvent.PathwayClicked ->
                sendEffect(PathwayListEffect.NavigateToDetail(event.pathwayId))
        }
    }
}