package com.mehmetbozkurt.questlog.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.mehmetbozkurt.questlog.core.database.entity.CategoryEntity
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
    )
}

fun CategoryEntity.toFireStoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "ownerId" to ownerId,
    "name" to name,
    "colorHex" to colorHex,
    "createdAtMillis" to createdAtMillis,
    "isDeleted" to isDeleted,
)

fun DocumentSnapshot.toCategoryEntityOrNull(): CategoryEntity? {
    val id = getString("id") ?: return null
    val ownerId = getString("ownerId") ?: return null
    val name = getString("name") ?: return null

    return CategoryEntity(
        id = id,
        ownerId = ownerId,
        name = name,
        colorHex = getString("colorHex") ?: "#C8A951",
        createdAtMillis = getLong("createdAtMillis") ?: 0L,
        isDeleted = getBoolean("isDeleted") ?: false,
        syncState = SyncState.SYNCED.name,
    )
}