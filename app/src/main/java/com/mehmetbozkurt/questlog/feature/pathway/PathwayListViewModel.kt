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
    repository: PathwayRepository,
) : MviViewModel<PathwayListState, PathwayListEvent, PathwayListEffect>(PathwayListState()) {

    init {
        combine(
            repository.observePathways(),
            repository.observeProgress(),
        ) { pathways, progressList ->
            val progressMap = progressList.associateBy { it.pathwayId }
            val titleMap = pathways.associate { it.id to it.title }

            pathways.map { pathway ->
                val required = pathway.requiredPathwayId
                val locked = required != null &&
                        progressMap[required]?.isCompleted != true

                PathwayListItem(
                    pathway = pathway,
                    progress = progressMap[pathway.id],
                    isLocked = locked,
                    requiredPathwayTitle = required?.let { titleMap[it] },
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