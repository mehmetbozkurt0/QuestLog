package com.mehmetbozkurt.questlog.data.remote

import com.mehmetbozkurt.questlog.core.database.entity.QuestLogEntity

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