package com.mehmetbozkurt.questlog.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.mehmetbozkurt.questlog.core.database.entity.PathwayEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayProgressEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayQuestEntity
import com.mehmetbozkurt.questlog.core.database.entity.QuestLogEntity
import com.mehmetbozkurt.questlog.core.database.entity.SyncState

fun QuestLogEntity.toFireStoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "ownerId" to ownerId,
    "campaignId" to campaignId,
    "type" to type,
    "title" to title,
    "description" to description,
    "categoryId" to categoryId,
    "priority" to priority,
    "dueAtMillis" to dueAtMillis,
    "remindAtMillis" to remindAtMillis,
    "isCompleted" to isCompleted,
    "createdAtMillis" to createdAtMillis,
    "updatedAtMillis" to updatedAtMillis,
    "isDeleted" to isDeleted,
    "readerIds" to listOf(ownerId),
    "editorIds" to listOf(ownerId),
    "pathwayQuestId" to pathwayQuestId,
)

fun DocumentSnapshot.toEntityOrNull(): QuestLogEntity? {
    val id = getString("id") ?: return null
    val ownerId = getString("ownerId") ?: return null
    val type = getString("type") ?: return null
    val title = getString("title") ?: return null

    return QuestLogEntity(
        id = id,
        ownerId = ownerId,
        campaignId = getString("campaignId"),
        type = type,
        title = title,
        description = getString("description").orEmpty(),
        categoryId = getString("categoryId"),
        priority = getString("priority"),
        dueAtMillis = getLong("dueAtMillis"),
        remindAtMillis = getLong("remindAtMillis"),
        isCompleted = getBoolean("isCompleted") ?: false,
        createdAtMillis = getLong("createdAtMillis") ?: 0L,
        updatedAtMillis = getLong("updatedAtMillis") ?: 0L,
        isDeleted = getBoolean("isDeleted") ?: false,
        syncState = SyncState.SYNCED.name,
        pathwayQuestId = getString("pathwayQuestId"),
    )
}

fun DocumentSnapshot.toPathwayEntityOrNull(): PathwayEntity? {
    val title = getString("title") ?: return null
    val primaryStat = getString("primaryStat") ?: return null

    return PathwayEntity(
        id = id,
        title = title,
        description = getString("description").orEmpty(),
        primaryStat = primaryStat,
        secondaryStat = getString("secondaryStat"),
        tier = (getLong("tier") ?: 1L).toInt(),
        requiredPathwayId = getString("requiredPathwayId"),
        completionBonusXp = (getLong("completionBonusXp") ?: 0L).toInt(),
        sortOrder = (getLong("order") ?: 0L).toInt(),
    )
}

fun DocumentSnapshot.toPathwayQuestEntityOrNull(pathwayId: String): PathwayQuestEntity? {
    val title = getString("title") ?: return null
    val statType = getString("statType") ?: return null
    val difficulty = getString("difficulty") ?: return null

    return PathwayQuestEntity(
        id = id,
        pathwayId = pathwayId,
        title = title,
        description = getString("description").orEmpty(),
        statType = statType,
        difficulty = difficulty,
        stage = (getLong("stage") ?: 1L).toInt(),
        requiredCompletions = (getLong("requiredCompletions") ?: 1L).toInt(),
        sortOrder = (getLong("order") ?: 0L).toInt(),
    )
}

fun PathwayProgressEntity.toFireStoreMap(): Map<String, Any?> = mapOf(
    "userId" to userId,
    "pathwayId" to pathwayId,
    "startedAtMillis" to startedAtMillis,
    "lastActivityAtMillis" to lastActivityAtMillis,
    "escrowedXp" to escrowedXp,
    "completedAtMillis" to completedAtMillis,
    "abandonedAtMillis" to abandonedAtMillis,
)