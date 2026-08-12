package com.mehmetbozkurt.questlog.core.database.dao

import androidx.room.Dao
import androidx.room.Query
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
}