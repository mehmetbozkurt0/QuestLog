package com.mehmetbozkurt.questlog.domain.model

import java.time.Instant

data class Crew(
    val crewId: String,
    val name: String,
    val inviteCode: String,
    val ownerId: String,
    val memberIds: List<String>,
)

data class CrewMember(
    val userId: String,
    val displayName: String,
    val level: Int,
    val totalXp: Int,
    val currentStreak: Int,
)

data class CrewFeedItem(
    val id: String,
    val authorId: String,
    val authorName: String,
    val title: String,
    val statType: StatType?,
    val difficulty: Difficulty?,
    val completedAt: Instant,
    val proofPhotoUrl: String?,
    val approvedBy: List<String>,
) {
    val approvalCount: Int get() = approvedBy.size
}

data class CrewState(
    val crew: Crew?,
    val members: List<CrewMember>,
    val feed: List<CrewFeedItem>,
) {
    companion object {
        val EMPTY = CrewState(null, emptyList(), emptyList())
    }
}
