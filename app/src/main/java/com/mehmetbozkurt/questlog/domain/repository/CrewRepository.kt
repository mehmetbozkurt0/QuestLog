package com.mehmetbozkurt.questlog.domain.repository

import com.mehmetbozkurt.questlog.domain.model.CrewState
import kotlinx.coroutines.flow.Flow

interface CrewRepository {
    fun observeCrewState(): Flow<CrewState>

    suspend fun createCrew(name: String): CrewActionResult
    suspend fun joinByCode(code: String): CrewActionResult
    suspend fun leaveCrew(): CrewActionResult
    suspend fun approve(entryId: String): ApproveResult
    suspend fun refreshMemberCard()
}

sealed interface CrewActionResult {
    data object Success : CrewActionResult
    data object AlreadyInCrew : CrewActionResult
    data object CodeNotFound : CrewActionResult
    data object NotInCrew : CrewActionResult
    data object Offline : CrewActionResult
    data class Failed(val message: String) : CrewActionResult
}

sealed interface ApproveResult {
    data class Granted(val xp: Int, val leveledUp: Boolean, val newLevel: Int) : ApproveResult
    data object NoMentorFeat : ApproveResult
    data object DailyLimitReached : ApproveResult
    data object AlreadyApproved : ApproveResult
    data object OwnQuest : ApproveResult
    data class Failed(val message: String) : ApproveResult
}
