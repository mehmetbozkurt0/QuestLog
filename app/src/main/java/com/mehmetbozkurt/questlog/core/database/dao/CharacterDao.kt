package com.mehmetbozkurt.questlog.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mehmetbozkurt.questlog.core.database.entity.CharacterEntity
import com.mehmetbozkurt.questlog.core.database.entity.FeatEntity
import com.mehmetbozkurt.questlog.core.database.entity.StatEntity
import com.mehmetbozkurt.questlog.core.database.entity.XpLedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters WHERE userId = :userId")
    fun observeCharacter(userId: String): Flow<CharacterEntity?>

    @Query("SELECT * FROM characters WHERE userId = :userId")
    suspend fun getCharacter(userId: String): CharacterEntity?

    @Upsert
    suspend fun upsertCharacter(entity: CharacterEntity)

    @Query("SELECT * FROM stats WHERE userId = :userId")
    fun observeStats(userId: String): Flow<List<StatEntity>>

    @Query("SELECT * FROM stats WHERE userId = :userId AND statType = :statType")
    suspend fun getStat(userId: String, statType: String): StatEntity?

    @Upsert
    suspend fun upsertStat(entity: StatEntity)

    @Upsert
    suspend fun upsertStats(entities: List<StatEntity>)

    @Query("SELECT * FROM feats WHERE userId = :userId")
    fun observeFeats(userId: String): Flow<List<FeatEntity>>

    @Query("SELECT * FROM feats WHERE userId = :userId")
    suspend fun getFeats(userId: String): List<FeatEntity>

    @Upsert
    suspend fun upsertFeat(entity: FeatEntity)

    @Upsert
    suspend fun insertLedger(entity: XpLedgerEntity)

    @Query("""
        SELECT COALESCE(SUM(finalXp), 0) FROM xp_ledger
        WHERE userId = :userId AND statType = :statType
          AND earnedAtMillis >= :sinceMillis
    """)
    suspend fun xpEarnedForStatSince(
        userId: String,
        statType: String,
        sinceMillis: Long,
    ): Int

    @Query("""
        SELECT DISTINCT statType FROM xp_ledger
        WHERE userId = :userId AND earnedAtMillis >= :sinceMillis
    """)
    suspend fun distinctStatsSince(userId: String, sinceMillis: Long): List<String>

    @Query("""
        SELECT COALESCE(SUM(finalXp), 0) FROM xp_ledger
        WHERE userId = :userId AND earnedAtMillis >= :sinceMillis
    """)
    fun observeXpSince(userId: String, sinceMillis: Long): Flow<Int>

    @Query("DELETE FROM xp_ledger WHERE logId = :logId")
    suspend fun deleteLedgerForLog(logId: String)

    @Query("SELECT * FROM characters WHERE syncState != 'SYNCED'")
    suspend fun getPendingCharacters(): List<CharacterEntity>

    @Query("SELECT * FROM stats WHERE syncState != 'SYNCED'")
    suspend fun getPendingStats(): List<StatEntity>

    @Query("SELECT * FROM feats WHERE syncState != 'SYNCED'")
    suspend fun getPendingFeats(): List<FeatEntity>
}































