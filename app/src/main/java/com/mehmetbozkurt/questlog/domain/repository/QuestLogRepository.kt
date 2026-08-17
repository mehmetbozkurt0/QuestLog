package com.mehmetbozkurt.questlog.domain.repository

import com.mehmetbozkurt.questlog.domain.model.QuestLog
import kotlinx.coroutines.flow.Flow

interface QuestLogRepository {
    fun observeAll(): Flow<List<QuestLog>>
    fun observeById(id: String): Flow<QuestLog?>
    suspend fun upsert(log: QuestLog)
    suspend fun setCompleted(id: String, completed: Boolean): XpAward?
    suspend fun delete(id: String)
    fun newId(): String
}