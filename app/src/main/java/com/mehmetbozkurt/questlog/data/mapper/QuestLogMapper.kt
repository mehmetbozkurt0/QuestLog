package com.mehmetbozkurt.questlog.data.mapper

import com.mehmetbozkurt.questlog.core.database.entity.QuestLogEntity
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.ProofLevel
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import com.mehmetbozkurt.questlog.domain.model.StatType
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
    statType = statType?.let { runCatching { StatType.valueOf(it) }.getOrNull() },
    difficulty = difficulty?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() },
    proofLevel = runCatching { ProofLevel.valueOf(proofLevel) }.getOrDefault(ProofLevel.NONE),
    proofNote = proofNote,
    proofPhotoUrl = proofPhotoUrl,
    proofPhotoLocalPath = proofPhotoLocalPath,
    completedAt = completedAtMillis?.let(Instant::ofEpochMilli),
    pathwayQuestId = pathwayQuestId,
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
        statType = statType?.name,
        difficulty = difficulty?.name,
        proofLevel = proofLevel.name,
        proofNote = proofNote,
        proofPhotoUrl = proofPhotoUrl,
        proofPhotoLocalPath = proofPhotoLocalPath,
        completedAtMillis = completedAt?.toEpochMilli(),
        syncState = syncState.name,
        pathwayQuestId = pathwayQuestId,
    )