package com.mehmetbozkurt.questlog.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetbozkurt.questlog.domain.progression.StreakInfo
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.CrewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AppHeaderState(
    val level: Int = 1,
    val levelProgress: Float = 0f,
    val streak: StreakInfo? = null,
)

@HiltViewModel
class AppShellViewModel @Inject constructor(
    crewRepository: CrewRepository,
    characterRepository: CharacterRepository,
) : ViewModel() {

    val unreadMessages: StateFlow<Int> = crewRepository.observeUnreadMessageCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val header: StateFlow<AppHeaderState> = combine(
        characterRepository.observeCharacter(),
        characterRepository.observeStreak(),
    ) { character, streak ->
        AppHeaderState(
            level = character?.level ?: 1,
            levelProgress = character?.let {
                if (it.xpToNextLevel <= 0) 1f
                else (it.xpIntoLevel.toFloat() / it.xpToNextLevel).coerceIn(0f, 1f)
            } ?: 0f,
            streak = streak,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppHeaderState())
}
