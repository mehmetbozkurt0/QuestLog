package com.mehmetbozkurt.questlog.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("ALTER TABLE quest_logs ADD COLUMN statType TEXT")
        db.execSQL("ALTER TABLE quest_logs ADD COLUMN difficulty TEXT")
        db.execSQL("ALTER TABLE quest_logs ADD COLUMN proofLevel TEXT NOT NULL DEFAULT 'NONE'")
        db.execSQL("ALTER TABLE quest_logs ADD COLUMN proofNote TEXT")
        db.execSQL("ALTER TABLE quest_logs ADD COLUMN completedAtMillis INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quest_logs_statType ON quest_logs(statType)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS characters (
                userId TEXT NOT NULL PRIMARY KEY,
                totalXp INTEGER NOT NULL DEFAULT 0,
                pendingFeatChoices INTEGER NOT NULL DEFAULT 0,
                createdAtMillis INTEGER NOT NULL,
                updatedAtMillis INTEGER NOT NULL,
                syncState TEXT NOT NULL DEFAULT 'PENDING'
            )
        """)

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS stats (
                userId TEXT NOT NULL,
                statType TEXT NOT NULL,
                value INTEGER NOT NULL DEFAULT 10,
                currentXp INTEGER NOT NULL DEFAULT 0,
                updatedAtMillis INTEGER NOT NULL,
                syncState TEXT NOT NULL DEFAULT 'PENDING',
                PRIMARY KEY(userId, statType)
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_stats_userId ON stats(userId)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS feats (
                id TEXT NOT NULL PRIMARY KEY,
                userId TEXT NOT NULL,
                featId TEXT NOT NULL,
                chosenStat TEXT,
                acquiredAtLevel INTEGER NOT NULL,
                acquiredAtMillis INTEGER NOT NULL,
                syncState TEXT NOT NULL DEFAULT 'PENDING'
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_feats_userId ON feats(userId)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS xp_ledger (
                id TEXT NOT NULL PRIMARY KEY,
                userId TEXT NOT NULL,
                logId TEXT NOT NULL,
                statType TEXT NOT NULL,
                baseXp INTEGER NOT NULL,
                finalXp INTEGER NOT NULL,
                earnedAtMillis INTEGER NOT NULL,
                syncState TEXT NOT NULL DEFAULT 'PENDING'
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_xp_ledger_userId ON xp_ledger(userId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_xp_ledger_earnedAtMillis ON xp_ledger(earnedAtMillis)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_xp_ledger_userId_statType_earnedAtMillis ON xp_ledger(userId, statType, earnedAtMillis)")
    }
}