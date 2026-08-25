package com.mehmetbozkurt.questlog.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.domain.repository.CrewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CrewBadgeViewModel @Inject constructor(
    crewRepository: CrewRepository,
) : ViewModel() {
    val unreadMessages: StateFlow<Int> = crewRepository.observeUnreadMessageCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
