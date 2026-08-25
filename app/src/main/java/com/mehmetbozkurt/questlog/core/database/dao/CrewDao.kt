package com.mehmetbozkurt.questlog.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mehmetbozkurt.questlog.core.database.entity.CrewEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewFeedEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewMemberEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CrewDao {
    @Query("SELECT * FROM crews WHERE crewId = :crewId")
    fun observeCrew(crewId: String): Flow<CrewEntity?>

    @Upsert
    suspend fun upsertCrew(entity: CrewEntity)

    @Query("DELETE FROM crews WHERE crewId = :crewId")
    suspend fun deleteCrew(crewId: String)

    @Query("SELECT * FROM crew_members WHERE crewId = :crewId ORDER BY totalXp DESC")
    fun observeMembers(crewId: String): Flow<List<CrewMemberEntity>>

    @Upsert
    suspend fun upsertMember(entity: CrewMemberEntity)

    @Upsert
    suspend fun upsertMembers(entities: List<CrewMemberEntity>)

    @Query("SELECT * FROM crew_members WHERE syncState != 'SYNCED'")
    suspend fun getPendingMembers(): List<CrewMemberEntity>

    @Query("DELETE FROM crew_members WHERE crewId = :crewId")
    suspend fun deleteMembersForCrew(crewId: String)

    @Query("SELECT * FROM crew_feed WHERE crewId = :crewId ORDER BY completedAtMillis DESC LIMIT :limit")
    fun observeFeed(crewId: String, limit: Int): Flow<List<CrewFeedEntity>>

    @Query("SELECT * FROM crew_feed WHERE id = :id")
    suspend fun getFeedEntry(id: String): CrewFeedEntity?

    @Upsert
    suspend fun upsertFeedEntry(entity: CrewFeedEntity)

    @Query("SELECT * FROM crew_feed WHERE syncState != 'SYNCED'")
    suspend fun getPendingFeedEntries(): List<CrewFeedEntity>

    @Query("""
        UPDATE crew_feed SET proofPhotoUrl = :url, syncState = 'PENDING'
        WHERE questLogId = :questLogId AND authorId = :authorId
    """)
    suspend fun setFeedProofPhotoUrl(questLogId: String, authorId: String, url: String)

    @Query("DELETE FROM crew_feed WHERE crewId = :crewId")
    suspend fun deleteFeedForCrew(crewId: String)

    @Query("SELECT * FROM crew_messages WHERE crewId = :crewId ORDER BY sentAtMillis DESC LIMIT :limit")
    fun observeMessages(crewId: String, limit: Int): Flow<List<CrewMessageEntity>>

    @Upsert
    suspend fun upsertMessage(entity: CrewMessageEntity)

    @Upsert
    suspend fun upsertMessages(entities: List<CrewMessageEntity>)

    @Query("SELECT * FROM crew_messages WHERE syncState != 'SYNCED'")
    suspend fun getPendingMessages(): List<CrewMessageEntity>

    @Query("DELETE FROM crew_messages WHERE crewId = :crewId")
    suspend fun deleteMessagesForCrew(crewId: String)
}
