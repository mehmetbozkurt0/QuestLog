package com.mehmetbozkurt.questlog.domain.repository

import com.mehmetbozkurt.questlog.domain.model.CrewMessage
import com.mehmetbozkurt.questlog.domain.model.CrewState
import kotlinx.coroutines.flow.Flow

interface CrewRepository {
    fun observeCrewState(): Flow<CrewState>

    fun observeMessages(): Flow<List<CrewMessage>>

    fun observeUnreadMessageCount(): Flow<Int>

    suspend fun sendMessage(text: String): CrewActionResult

    suspend fun createCrew(name: String): CrewActionResult
    suspend fun joinByCode(code: String): CrewActionResult
    suspend fun leaveCrew(): CrewActionResult
    suspend fun kickMember(userId: String): CrewActionResult
    suspend fun transferOwnership(userId: String): CrewActionResult
    suspend fun renameCrew(name: String): CrewActionResult
    suspend fun regenerateInviteCode(): CrewActionResult
    suspend fun handleEviction()
    suspend fun approve(entryId: String): ApproveResult
    suspend fun refreshMemberCard()
}

sealed interface CrewActionResult {
    data object Success : CrewActionResult
    data object AlreadyInCrew : CrewActionResult
    data object CodeNotFound : CrewActionResult
    data object NotInCrew : CrewActionResult
    data object NotOwner : CrewActionResult
    data object InvalidName : CrewActionResult
    data object MemberNotFound : CrewActionResult
    data object Offline : CrewActionResult
    data class Failed(val reason: CrewFailure) : CrewActionResult
}

enum class CrewFailure {
    NO_SESSION,
    NO_CHARACTER,
    PERMISSION_DENIED,
    UNKNOWN,
}

sealed interface ApproveResult {
    data class Granted(val xp: Int, val leveledUp: Boolean, val newLevel: Int) : ApproveResult
    data object NoMentorFeat : ApproveResult
    data object DailyLimitReached : ApproveResult
    data object AlreadyApproved : ApproveResult
    data object OwnQuest : ApproveResult
    data class Failed(val reason: ApproveFailure) : ApproveResult
}

enum class ApproveFailure {
    NO_SESSION,
    NO_CHARACTER,
    NOT_IN_CREW,
    ENTRY_NOT_FOUND,
    WRITE_FAILED,
}
