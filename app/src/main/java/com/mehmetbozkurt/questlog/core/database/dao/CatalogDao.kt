package com.mehmetbozkurt.questlog.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mehmetbozkurt.questlog.core.database.entity.CatalogCompletionEntity
import com.mehmetbozkurt.questlog.core.database.entity.CatalogTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {

    @Query("SELECT * FROM catalog_tasks ORDER BY sortOrder")
    fun observeTasks(): Flow<List<CatalogTaskEntity>>

    @Query("SELECT * FROM catalog_tasks WHERE id = :id")
    suspend fun getTask(id: String): CatalogTaskEntity?

    @Upsert
    suspend fun upsertTasks(entities: List<CatalogTaskEntity>)

    @Query("DELETE FROM catalog_tasks")
    suspend fun deleteAllTasks()

    @Transaction
    suspend fun replaceTasks(entities: List<CatalogTaskEntity>) {
        deleteAllTasks()
        upsertTasks(entities)
    }

    @Query("SELECT * FROM catalog_completions WHERE userId = :userId")
    fun observeCompletions(userId: String): Flow<List<CatalogCompletionEntity>>

    @Query("SELECT * FROM catalog_completions WHERE userId = :userId AND taskId = :taskId")
    suspend fun getCompletion(userId: String, taskId: String): CatalogCompletionEntity?

    @Query("""
        SELECT COUNT(*) FROM catalog_completions
        WHERE userId = :userId AND lastCompletedAtMillis >= :sinceMillis
    """)
    suspend fun completionCountSince(userId: String, sinceMillis: Long): Int

    @Upsert
    suspend fun upsertCompletion(entity: CatalogCompletionEntity)

    @Query("SELECT * FROM catalog_completions WHERE syncState != 'SYNCED'")
    suspend fun getPendingCompletions(): List<CatalogCompletionEntity>
}
