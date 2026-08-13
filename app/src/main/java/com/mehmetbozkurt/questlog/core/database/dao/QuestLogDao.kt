package com.mehmetbozkurt.questlog.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mehmetbozkurt.questlog.core.database.entity.QuestLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestLogDao {

    @Query("""
        SELECT * FROM quest_logs
        WHERE ownerId = :ownerId AND isDeleted = 0
        ORDER BY createdAtMillis DESC
    """)
    fun observeAll(ownerId: String): Flow<List<QuestLogEntity>>

    @Query("SELECT * FROM quest_logs WHERE id = :id AND isDeleted = 0")
    fun observeById(id: String): Flow<QuestLogEntity?>

    @Query("SELECT * FROM quest_logs WHERE id = :id")
    suspend fun getById(id: String): QuestLogEntity?

    @Upsert
    suspend fun upsert(entity: QuestLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<QuestLogEntity>)

    @Query("""
        UPDATE quest_logs
        SET isDeleted = 1, updatedAtMillis = :nowMillis, syncState = 'PENDING'
        WHERE id = :id
    """)
    suspend fun softDelete(id: String, nowMillis: Long)

    @Query("""
        UPDATE quest_logs
        SET isCompleted = :completed, updatedAtMillis = :nowMillis, syncState = 'PENDING'
        WHERE id = :id
    """)
    suspend fun setCompleted(id: String, completed: Boolean, nowMillis: Long)

    @Query("SELECT * FROM quest_logs WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<QuestLogEntity>

    @Query("UPDATE quest_logs SET syncState = :state WHERE id = :id")
    suspend fun updateSyncState(id: String, state: String)

    @Query("""
    UPDATE quest_logs SET
        ownerId = :ownerId,
        campaignId = :campaignId,
        type = :type,
        title = :title,
        description = :description,
        categoryId = :categoryId,
        priority = :priority,
        dueAtMillis = :dueAtMillis,
        remindAtMillis = :remindAtMillis,
        isCompleted = :isCompleted,
        createdAtMillis = :createdAtMillis,
        updatedAtMillis = :updatedAtMillis,
        isDeleted = :isDeleted,
        syncState = 'SYNCED'
    WHERE id = :id
      AND syncState = 'SYNCED'
      AND updatedAtMillis < :updatedAtMillis
""")
    suspend fun updateFromRemoteIfNewer(
        id: String,
        ownerId: String,
        campaignId: String?,
        type: String,
        title: String,
        description: String,
        categoryId: String?,
        priority: String?,
        dueAtMillis: Long?,
        remindAtMillis: Long?,
        isCompleted: Boolean,
        createdAtMillis: Long,
        updatedAtMillis: Long,
        isDeleted: Boolean,
    )

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entity: QuestLogEntity): Long

    @Transaction
    suspend fun mergeFromRemote(entities: List<QuestLogEntity>) {
        entities.forEach { e ->
            val inserted = insertIfAbsent(e)
            if (inserted == -1L) {
                updateFromRemoteIfNewer(
                    id = e.id,
                    ownerId = e.ownerId,
                    campaignId = e.campaignId,
                    type = e.type,
                    title = e.title,
                    description = e.description,
                    categoryId = e.categoryId,
                    priority = e.priority,
                    dueAtMillis = e.dueAtMillis,
                    remindAtMillis = e.remindAtMillis,
                    isCompleted = e.isCompleted,
                    createdAtMillis = e.createdAtMillis,
                    updatedAtMillis = e.updatedAtMillis,
                    isDeleted = e.isDeleted,
                )
            }
        }
    }
}