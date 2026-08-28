package com.mehmetbozkurt.questlog.feature.crew.member

import com.mehmetbozkurt.questlog.core.common.mvi.UiEffect
import com.mehmetbozkurt.questlog.core.common.mvi.UiEvent
import com.mehmetbozkurt.questlog.core.common.mvi.UiState
import com.mehmetbozkurt.questlog.domain.model.CrewFeedItem
import com.mehmetbozkurt.questlog.domain.model.CrewMember
import com.mehmetbozkurt.questlog.domain.progression.XpCurve

data class CrewMemberState(
    val member: CrewMember? = null,
    val rank: Int = 0,
    val crewSize: Int = 0,
    val feed: List<CrewFeedItem> = emptyList(),
    val isSelf: Boolean = false,
    val isLoading: Boolean = true,
) : UiState {

    private val levelInfo get() = member?.let { XpCurve.levelFromTotalXp(it.totalXp) }

    val levelProgress: Float
        get() {
            val info = levelInfo ?: return 0f
            if (info.xpToNextLevel <= 0) return 1f
            return (info.xpIntoLevel.toFloat() / info.xpToNextLevel).coerceIn(0f, 1f)
        }

    val xpIntoLevel: Int get() = levelInfo?.xpIntoLevel ?: 0

    val xpToNextLevel: Int get() = levelInfo?.xpToNextLevel ?: 0

    val isMaxLevel: Boolean get() = (member?.level ?: 1) >= XpCurve.MAX_LEVEL

    val questsShared: Int get() = feed.size

    val approvalsReceived: Int get() = feed.sumOf { it.approvalCount }
}

sealed interface CrewMemberEvent : UiEvent

sealed interface CrewMemberEffect : UiEffect
