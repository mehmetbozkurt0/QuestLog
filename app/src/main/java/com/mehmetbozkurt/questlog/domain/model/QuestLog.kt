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
    val categoryId: String?,
    val priority: Priority?,
    val dueAt: Instant?,
    val remindAt: Instant?,
    val isCompleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val descriptionFirstLine: String
        get() = description.lineSequence().firstOrNull()?.trim().orEmpty()
}