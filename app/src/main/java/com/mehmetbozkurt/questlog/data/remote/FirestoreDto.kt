package com.mehmetbozkurt.questlog.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.mehmetbozkurt.questlog.core.database.entity.CharacterEntity
import com.mehmetbozkurt.questlog.core.database.entity.FeatEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayProgressEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayQuestCompletionEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayQuestEntity
import com.mehmetbozkurt.questlog.core.database.entity.QuestLogEntity
import com.mehmetbozkurt.questlog.core.database.entity.StatEntity
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.core.database.entity.XpLedgerEntity

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
    "statType" to statType,
    "difficulty" to difficulty,
    "proofLevel" to proofLevel,
    "proofNote" to proofNote,
    "proofPhotoUrl" to proofPhotoUrl,
    "completedAtMillis" to completedAtMillis,
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
        statType = getString("statType"),
        difficulty = getString("difficulty"),
        proofLevel = getString("proofLevel") ?: "NONE",
        proofNote = getString("proofNote"),
        proofPhotoUrl = getString("proofPhotoUrl"),
        completedAtMillis = getLong("completedAtMillis"),
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

fun CharacterEntity.toFireStoreMap(): Map<String, Any?> = mapOf(
    "userId" to userId,
    "totalXp" to totalXp,
    "pendingFeatChoices" to pendingFeatChoices,
    "createdAtMillis" to createdAtMillis,
    "updatedAtMillis" to updatedAtMillis,
    "crewId" to crewId,
    "crewJoinedAtMillis" to crewJoinedAtMillis,
)

fun DocumentSnapshot.toCharacterEntityOrNull(): CharacterEntity? {
    val userId = getString("userId") ?: return null
    return CharacterEntity(
        userId = userId,
        totalXp = (getLong("totalXp") ?: 0L).toInt(),
        pendingFeatChoices = (getLong("pendingFeatChoices") ?: 0L).toInt(),
        createdAtMillis = getLong("createdAtMillis") ?: 0L,
        updatedAtMillis = getLong("updatedAtMillis") ?: 0L,
        syncState = SyncState.SYNCED.name,
        crewId = getString("crewId"),
        crewJoinedAtMillis = getLong("crewJoinedAtMillis"),
    )
}

fun StatEntity.toFireStoreMap(): Map<String, Any?> = mapOf(
    "statType" to statType,
    "value" to value,
    "currentXp" to currentXp,
    "updatedAtMillis" to updatedAtMillis,
)

fun DocumentSnapshot.toStatEntityOrNull(userId: String): StatEntity? {
    val statType = getString("statType") ?: return null
    return StatEntity(
        userId = userId,
        statType = statType,
        value = (getLong("value") ?: 10L).toInt(),
        currentXp = (getLong("currentXp") ?: 0L).toInt(),
        updatedAtMillis = getLong("updatedAtMillis") ?: 0L,
        syncState = SyncState.SYNCED.name,
    )
}

fun FeatEntity.toFireStoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "featId" to featId,
    "chosenStat" to chosenStat,
    "acquiredAtLevel" to acquiredAtLevel,
    "acquiredAtMillis" to acquiredAtMillis,
)

fun DocumentSnapshot.toFeatEntityOrNull(userId: String): FeatEntity? {
    val featId = getString("featId") ?: return null
    return FeatEntity(
        id = id,
        userId = userId,
        featId = featId,
        chosenStat = getString("chosenStat"),
        acquiredAtLevel = (getLong("acquiredAtLevel") ?: 1L).toInt(),
        acquiredAtMillis = getLong("acquiredAtMillis") ?: 0L,
        syncState = SyncState.SYNCED.name,
    )
}

fun XpLedgerEntity.toFireStoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "logId" to logId,
    "statType" to statType,
    "baseXp" to baseXp,
    "finalXp" to finalXp,
    "earnedAtMillis" to earnedAtMillis,
)

fun DocumentSnapshot.toLedgerEntityOrNull(userId: String): XpLedgerEntity? {
    val logId = getString("logId") ?: return null
    val statType = getString("statType") ?: return null
    return XpLedgerEntity(
        id = id,
        userId = userId,
        logId = logId,
        statType = statType,
        baseXp = (getLong("baseXp") ?: 0L).toInt(),
        finalXp = (getLong("finalXp") ?: 0L).toInt(),
        earnedAtMillis = getLong("earnedAtMillis") ?: 0L,
        syncState = SyncState.SYNCED.name,
    )
}

fun PathwayQuestCompletionEntity.toFireStoreMap(): Map<String, Any?> = mapOf(
    "questId" to questId,
    "completions" to completions,
    "lastCompletedAtMillis" to lastCompletedAtMillis,
)

fun DocumentSnapshot.toCompletionEntityOrNull(userId: String): PathwayQuestCompletionEntity? {
    val questId = getString("questId") ?: return null
    return PathwayQuestCompletionEntity(
        userId = userId,
        questId = questId,
        completions = (getLong("completions") ?: 0L).toInt(),
        lastCompletedAtMillis = getLong("lastCompletedAtMillis") ?: 0L,
        syncState = SyncState.SYNCED.name,
    )
}