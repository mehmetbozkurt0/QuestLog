package com.mehmetbozkurt.questlog.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mehmetbozkurt.questlog.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("""
        SELECT * FROM categories
        WHERE ownerId = :ownerId AND isDeleted = 0
        ORDER BY name COLLATE NOCASE ASC
    """)
    fun observeAll(ownerId: String): Flow<List<CategoryEntity>>

    @Upsert
    suspend fun upsert(entity: CategoryEntity)

    @Query("""
        UPDATE categories SET isDeleted = 1, syncState = 'PENDING' WHERE id = :id
    """)
    suspend fun softDelete(id: String)

    @Query("SELECT COUNT(*) FROM categories WHERE ownerId = :ownerId AND isDeleted = 0")
    suspend fun count(ownerId: String): Int

    @Query("SELECT * FROM categories WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<CategoryEntity>

    @Query("UPDATE categories SET syncState = :state WHERE id = :id")
    suspend fun updateSyncState(id: String, state: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entity: CategoryEntity): Long

    @Query("""
        UPDATE categories SET
            name = :name,
            colorHex = :colorHex,
            isDeleted = :isDeleted,
            syncState = 'SYNCED'
        WHERE id = :id AND syncState = 'SYNCED'
    """)
    suspend fun updateFromRemote(
        id: String,
        name: String,
        colorHex: String,
        isDeleted: Boolean,
    )

    @Transaction
    suspend fun mergeFromRemote(entities: List<CategoryEntity>) {
        entities.forEach { e ->
            if (insertIfAbsent(e) == -1L) {
                updateFromRemote(e.id, e.name, e.colorHex, e.isDeleted)
            }
        }
    }
}