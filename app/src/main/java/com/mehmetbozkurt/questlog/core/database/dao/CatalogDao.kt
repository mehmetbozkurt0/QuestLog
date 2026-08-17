package com.mehmetbozkurt.questlog.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mehmetbozkurt.questlog.core.database.entity.CatalogQuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {

    @Query("SELECT * FROM catalog_quests ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<CatalogQuestEntity>>

    @Query("SELECT COUNT(*) FROM catalog_quests")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CatalogQuestEntity>)

    @Query("DELETE FROM catalog_quests")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(entities: List<CatalogQuestEntity>) {
        deleteAll()
        insertAll(entities)
    }
}