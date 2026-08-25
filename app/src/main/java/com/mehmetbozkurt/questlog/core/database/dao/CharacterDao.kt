package com.mehmetbozkurt.questlog.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mehmetbozkurt.questlog.core.database.entity.CharacterEntity
import com.mehmetbozkurt.questlog.core.database.entity.FeatEntity
import com.mehmetbozkurt.questlog.core.database.entity.PendingDeletionEntity
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

    @Query("DELETE FROM xp_ledger WHERE logId = :logId")
    suspend fun deleteLedgerForLog(logId: String)

    @Query("SELECT * FROM characters WHERE syncState != 'SYNCED'")
    suspend fun getPendingCharacters(): List<CharacterEntity>

    @Query("SELECT * FROM stats WHERE syncState != 'SYNCED'")
    suspend fun getPendingStats(): List<StatEntity>

    @Query("SELECT * FROM feats WHERE syncState != 'SYNCED'")
    suspend fun getPendingFeats(): List<FeatEntity>

    @Query("SELECT earnedAtMillis FROM xp_ledger WHERE userId = :userId")
    fun observeLedgerTimes(userId: String): Flow<List<Long>>

    @Query("SELECT * FROM xp_ledger WHERE userId = :userId AND earnedAtMillis >= :sinceMillis")
    fun observeLedgerSince(userId: String, sinceMillis: Long): Flow<List<XpLedgerEntity>>

    @Query("SELECT earnedAtMillis FROM xp_ledger WHERE userId = :userId")
    suspend fun getLedgerTimes(userId: String): List<Long>

    @Query("SELECT COUNT(*) FROM xp_ledger WHERE userId = :userId AND earnedAtMillis >= :sinceMillis")
    suspend fun ledgerCountSince(userId: String, sinceMillis: Long): Int

    @Query("""
        SELECT COUNT(*) FROM xp_ledger
        WHERE userId = :userId AND logId = :logId
        AND earnedAtMillis >= :sinceMillis
    """)
    suspend fun ledgerCountForLogSince(
        userId: String,
        logId: String,
        sinceMillis: Long,
    ): Int

    @Query("""
        SELECT COUNT(*) FROM xp_ledger l
        INNER JOIN quest_logs q ON q.id = l.logId
        WHERE l.userId = :userId AND q.difficulty = :difficulty
        AND l.earnedAtMillis >= :sinceMillis
    """)
    suspend fun ledgerCountForDifficultySince(
        userId: String,
        difficulty: String,
        sinceMillis: Long,
    ): Int

    @Query("SELECT * FROM xp_ledger WHERE userId = :userId AND logId = :logId")
    suspend fun ledgerEntriesForLog(userId: String, logId: String): List<XpLedgerEntity>

    @Query("""
        SELECT * FROM xp_ledger
        WHERE userId = :userId AND logId = :logId AND earnedAtMillis >= :sinceMillis
    """)
    suspend fun ledgerEntriesForLogSince(
        userId: String,
        logId: String,
        sinceMillis: Long,
    ): List<XpLedgerEntity>

    @Query("SELECT * FROM xp_ledger WHERE syncState != 'SYNCED'")
    suspend fun getPendingLedger(): List<XpLedgerEntity>

    @Upsert
    suspend fun upsertLedgerEntries(entities: List<XpLedgerEntity>)

    @Upsert
    suspend fun upsertFeats(entities: List<FeatEntity>)

    @Query("SELECT * FROM pending_deletions")
    suspend fun getPendingDeletions(): List<PendingDeletionEntity>

    @Query("SELECT docId FROM pending_deletions")
    suspend fun getPendingDeletionIds(): List<String>

    @Upsert
    suspend fun insertPendingDeletions(entities: List<PendingDeletionEntity>)

    @Query("DELETE FROM pending_deletions WHERE docId = :docId")
    suspend fun clearPendingDeletion(docId: String)
}































