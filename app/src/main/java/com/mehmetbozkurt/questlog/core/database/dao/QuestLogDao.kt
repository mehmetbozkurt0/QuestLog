package com.mehmetbozkurt.questlog.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
}