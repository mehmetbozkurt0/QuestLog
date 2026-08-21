package com.mehmetbozkurt.questlog.domain.model

import java.time.Instant

enum class LogType {QUEST, NPC, LORE, SESSION_NOTE}

enum class Priority {LOW, MEDIUM, HIGH}

data class QuestLog (
    val id: String,
    val ownerId: String,
    val campaignId: String?,
    val type: LogType,
    val title: String,
    val description: String,
    val categoryId: String? = null,
    val priority: Priority?,
    val dueAt: Instant?,
    val remindAt: Instant?,
    val isCompleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val statType: StatType?,
    val pathwayQuestId: String?,
    val difficulty: Difficulty?,
    val proofLevel: ProofLevel,
    val proofNote: String?,
    val proofPhotoUrl: String? = null,
    val proofPhotoLocalPath: String? = null,
    val completedAt: Instant?,
) {
    val descriptionFirstLine: String
        get() = description.lineSequence().firstOrNull()?.trim().orEmpty()

    val isXpEligible: Boolean
        get() = type == LogType.QUEST && statType != null && difficulty != null
}