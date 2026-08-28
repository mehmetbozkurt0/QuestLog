package com.mehmetbozkurt.questlog.feature.crew.member

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mehmetbozkurt.questlog.core.common.mvi.MviViewModel
import com.mehmetbozkurt.questlog.core.navigation.CrewMemberRouteKey
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.CrewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CrewMemberViewModel @Inject constructor(
    crewRepository: CrewRepository,
    authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<CrewMemberState, CrewMemberEvent, CrewMemberEffect>(CrewMemberState()) {

    private val userId = savedStateHandle.toRoute<CrewMemberRouteKey>().userId

    init {
        val ownId = authRepository.currentUserSync()?.uid

        crewRepository.observeCrewState()
            .onEach { crew ->
                val ordered = crew.members.sortedByDescending { it.totalXp }
                val index = ordered.indexOfFirst { it.userId == userId }
                setState {
                    copy(
                        member = ordered.getOrNull(index),
                        rank = if (index >= 0) index + 1 else 0,
                        crewSize = ordered.size,
                        feed = crew.feed.filter { it.authorId == userId },
                        isSelf = userId == ownId,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: CrewMemberEvent) = Unit
}
