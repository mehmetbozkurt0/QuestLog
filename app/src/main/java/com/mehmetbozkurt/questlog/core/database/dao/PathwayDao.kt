package com.mehmetbozkurt.questlog.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mehmetbozkurt.questlog.core.database.entity.PathwayEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayProgressEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayQuestCompletionEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayQuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PathwayDao {
    @Query("SELECT * FROM pathways ORDER BY sortOrder ASC")
    fun observePathways(): Flow<List<PathwayEntity>>

    @Query("SELECT * FROM pathways WHERE id = :id")
    suspend fun getPathway(id: String): PathwayEntity?

    @Query("SELECT * FROM pathway_quests WHERE pathwayId = :pathwayId ORDER BY stage ASC, sortOrder ASC")
    fun observeQuestsFor(pathwayId: String): Flow<List<PathwayQuestEntity>>

    @Query("SELECT * FROM pathway_quests WHERE pathwayId = :pathwayId")
    suspend fun getQuestsFor(pathwayId: String): List<PathwayQuestEntity>

    @Query("SELECT * FROM pathway_quests WHERE id = :id")
    suspend fun getQuest(id: String): PathwayQuestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPathways(entities: List<PathwayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(entities: List<PathwayQuestEntity>)

    @Query("DELETE FROM pathways")
    suspend fun deleteAllPathways()

    @Query("DELETE FROM pathway_quests")
    suspend fun deleteAllQuests()

    @Transaction
    suspend fun replaceCatalog(
        pathways: List<PathwayEntity>,
        quests: List<PathwayQuestEntity>,
    ) {
        deleteAllPathways()
        deleteAllQuests()
        insertPathways(pathways)
        insertQuests(quests)
    }


    @Query("SELECT * FROM pathway_progress WHERE userId = :userId")
    fun observeProgress(userId: String): Flow<List<PathwayProgressEntity>>

    @Query("SELECT * FROM pathway_progress WHERE userId = :userId AND pathwayId = :pathwayId")
    suspend fun getProgress(userId: String, pathwayId: String): PathwayProgressEntity?

    @Query("""
        SELECT COUNT(*) FROM pathway_progress
        WHERE userId = :userId AND completedAtMillis IS NULL AND abandonedAtMillis IS NULL
    """)
    suspend fun activePathwayCount(userId: String): Int

    @Query("""
        SELECT * FROM pathway_progress
        WHERE userId = :userId AND completedAtMillis IS NULL AND abandonedAtMillis IS NULL
    """)
    suspend fun getActiveProgress(userId: String): List<PathwayProgressEntity>

    @Upsert
    suspend fun upsertProgress(entity: PathwayProgressEntity)

    @Query("SELECT * FROM pathway_quest_completions WHERE userId = :userId")
    fun observeCompletions(userId: String): Flow<List<PathwayQuestCompletionEntity>>

    @Query("SELECT * FROM pathway_quest_completions WHERE userId = :userId AND questId = :questId")
    suspend fun getCompletion(userId: String, questId: String): PathwayQuestCompletionEntity?

    @Upsert
    suspend fun upsertCompletion(entity: PathwayQuestCompletionEntity)

    @Query("DELETE FROM pathway_quest_completions WHERE userId = :userId AND questId IN (:questIds)")
    suspend fun deleteCompletions(userId: String, questIds: List<String>)


    @Query("SELECT * FROM pathway_progress WHERE syncState != 'SYNCED'")
    suspend fun getPendingProgress(): List<PathwayProgressEntity>

    @Query("SELECT * FROM pathway_quest_completions WHERE syncState != 'SYNCED'")
    suspend fun getPendingCompletions(): List<PathwayQuestCompletionEntity>

    @Query("SELECT * FROM pathway_quest_completions WHERE userId = :userId")
    suspend fun observeCompletionsSnapshot(userId: String): List<PathwayQuestCompletionEntity>
}