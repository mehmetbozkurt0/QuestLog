package com.mehmetbozkurt.questlog.data.mapper

import com.mehmetbozkurt.questlog.core.database.entity.CategoryEntity
import com.mehmetbozkurt.questlog.core.database.entity.QuestLogEntity
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.domain.model.Category
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import java.time.Instant

fun QuestLogEntity.toDomain(): QuestLog = QuestLog(
    id = id,
    ownerId = ownerId,
    campaignId = campaignId,
    type = LogType.valueOf(type),
    title = title,
    description = description,
    categoryId = categoryId,
    priority = priority?.let { Priority.valueOf(it) },
    dueAt = dueAtMillis?.let(Instant::ofEpochMilli),
    remindAt = remindAtMillis?.let(Instant::ofEpochMilli),
    isCompleted = isCompleted,
    createdAt = Instant.ofEpochMilli(createdAtMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtMillis),
)

fun QuestLog.toEntity(syncState: SyncState = SyncState.PENDING): QuestLogEntity =
    QuestLogEntity(
        id = id,
        ownerId = ownerId,
        campaignId = campaignId,
        type = type.name,
        title = title,
        description = description,
        categoryId = categoryId,
        priority = priority?.name,
        dueAtMillis = dueAt?.toEpochMilli(),
        remindAtMillis = remindAt?.toEpochMilli(),
        isCompleted = isCompleted,
        createdAtMillis = createdAt.toEpochMilli(),
        updatedAtMillis = updatedAt.toEpochMilli(),
        syncState = syncState.name,
    )

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    ownerId = ownerId,
    name = name,
    colorHex = colorHex,
    createdAt = Instant.ofEpochMilli(createdAtMillis),
)

fun Category.toEntity(syncState: SyncState = SyncState.PENDING): CategoryEntity =
    CategoryEntity(
        id = id,
        ownerId = ownerId,
        name = name,
        colorHex = colorHex,
        createdAtMillis = createdAt.toEpochMilli(),
        syncState = syncState.name,
    )