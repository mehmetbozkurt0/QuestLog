package com.mehmetbozkurt.questlog.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.mehmetbozkurt.questlog.core.database.entity.CrewEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewFeedEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewMemberEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewMessageEntity
import com.mehmetbozkurt.questlog.core.database.entity.SyncState

fun CrewEntity.toFireStoreMap(): Map<String, Any?> = mapOf(
    "id" to crewId,
    "name" to name,
    "inviteCode" to inviteCode,
    "ownerId" to ownerId,
    "memberIds" to memberIds,
    "createdAtMillis" to updatedAtMillis,
)

fun DocumentSnapshot.toCrewEntityOrNull(): CrewEntity? {
    val crewId = getString("id") ?: return null
    val memberIds = (get("memberIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    return CrewEntity(
        crewId = crewId,
        name = getString("name").orEmpty(),
        inviteCode = getString("inviteCode").orEmpty(),
        ownerId = getString("ownerId").orEmpty(),
        memberIdsCsv = memberIds.joinToString(","),
        updatedAtMillis = getLong("createdAtMillis") ?: 0L,
    )
}

fun CrewMemberEntity.toFireStoreMap(): Map<String, Any?> = mapOf(
    "uid" to userId,
    "displayName" to displayName,
    "level" to level,
    "totalXp" to totalXp,
    "currentStreak" to currentStreak,
    "photoUrl" to photoUrl,
    "updatedAtMillis" to updatedAtMillis,
)

fun DocumentSnapshot.toCrewMemberEntityOrNull(crewId: String): CrewMemberEntity? {
    val uid = getString("uid") ?: return null
    return CrewMemberEntity(
        userId = uid,
        crewId = crewId,
        displayName = getString("displayName").orEmpty(),
        level = (getLong("level") ?: 1L).toInt(),
        totalXp = (getLong("totalXp") ?: 0L).toInt(),
        currentStreak = (getLong("currentStreak") ?: 0L).toInt(),
        photoUrl = getString("photoUrl"),
        updatedAtMillis = getLong("updatedAtMillis") ?: 0L,
        syncState = SyncState.SYNCED.name,
    )
}

fun CrewFeedEntity.toFireStoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "authorId" to authorId,
    "authorName" to authorName,
    "questLogId" to questLogId,
    "title" to title,
    "statType" to statType,
    "difficulty" to difficulty,
    "completedAtMillis" to completedAtMillis,
    "proofPhotoUrl" to proofPhotoUrl,
    "approvedBy" to approvedBy,
)

fun DocumentSnapshot.toCrewFeedEntityOrNull(crewId: String): CrewFeedEntity? {
    val entryId = getString("id") ?: return null
    val approvedBy = (get("approvedBy") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    return CrewFeedEntity(
        id = entryId,
        crewId = crewId,
        authorId = getString("authorId").orEmpty(),
        authorName = getString("authorName").orEmpty(),
        questLogId = getString("questLogId").orEmpty(),
        title = getString("title").orEmpty(),
        statType = getString("statType"),
        difficulty = getString("difficulty"),
        completedAtMillis = getLong("completedAtMillis") ?: 0L,
        proofPhotoUrl = getString("proofPhotoUrl"),
        approvedByCsv = approvedBy.joinToString(","),
        syncState = SyncState.SYNCED.name,
    )
}

fun CrewMessageEntity.toFireStoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "authorId" to authorId,
    "authorName" to authorName,
    "text" to text,
    "sentAtMillis" to sentAtMillis,
)

fun DocumentSnapshot.toCrewMessageEntityOrNull(crewId: String): CrewMessageEntity? {
    val messageId = getString("id") ?: return null
    return CrewMessageEntity(
        id = messageId,
        crewId = crewId,
        authorId = getString("authorId").orEmpty(),
        authorName = getString("authorName").orEmpty(),
        text = getString("text").orEmpty(),
        sentAtMillis = getLong("sentAtMillis") ?: 0L,
        syncState = SyncState.SYNCED.name,
    )
}
