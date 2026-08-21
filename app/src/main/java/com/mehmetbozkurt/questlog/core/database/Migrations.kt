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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS catalog_quests (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                statType TEXT NOT NULL,
                difficulty TEXT NOT NULL,
                sortOrder INTEGER NOT NULL
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_catalog_quests_statType ON catalog_quests(statType)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS pathways (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                primaryStat TEXT NOT NULL,
                secondaryStat TEXT,
                tier INTEGER NOT NULL,
                requiredPathwayId TEXT,
                completionBonusXp INTEGER NOT NULL,
                sortOrder INTEGER NOT NULL
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pathways_primaryStat ON pathways(primaryStat)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS pathway_quests (
                id TEXT NOT NULL PRIMARY KEY,
                pathwayId TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                statType TEXT NOT NULL,
                difficulty TEXT NOT NULL,
                stage INTEGER NOT NULL,
                requiredCompletions INTEGER NOT NULL,
                sortOrder INTEGER NOT NULL
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pathway_quests_pathwayId ON pathway_quests(pathwayId)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS pathway_progress (
                userId TEXT NOT NULL,
                pathwayId TEXT NOT NULL,
                startedAtMillis INTEGER NOT NULL,
                lastActivityAtMillis INTEGER NOT NULL,
                escrowedXp INTEGER NOT NULL,
                completedAtMillis INTEGER,
                abandonedAtMillis INTEGER,
                syncState TEXT NOT NULL DEFAULT 'PENDING',
                PRIMARY KEY(userId, pathwayId)
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pathway_progress_userId ON pathway_progress(userId)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS pathway_quest_completions (
                userId TEXT NOT NULL,
                questId TEXT NOT NULL,
                completions INTEGER NOT NULL,
                lastCompletedAtMillis INTEGER NOT NULL,
                syncState TEXT NOT NULL DEFAULT 'PENDING',
                PRIMARY KEY(userId, questId)
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pathway_quest_completions_userId ON pathway_quest_completions(userId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pathway_quest_completions_questId ON pathway_quest_completions(questId)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE quest_logs ADD COLUMN pathwayQuestId TEXT")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS catalog_quests")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS pending_deletions (
                docId TEXT NOT NULL PRIMARY KEY,
                collection TEXT NOT NULL,
                userId TEXT NOT NULL
            )
        """)
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE characters ADD COLUMN crewId TEXT")
        db.execSQL("ALTER TABLE characters ADD COLUMN crewJoinedAtMillis INTEGER")
        db.execSQL("ALTER TABLE characters ADD COLUMN approvalDayMillis INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE characters ADD COLUMN approvalsToday INTEGER NOT NULL DEFAULT 0")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS crews (
                crewId TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                inviteCode TEXT NOT NULL,
                ownerId TEXT NOT NULL,
                memberIdsCsv TEXT NOT NULL,
                updatedAtMillis INTEGER NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS crew_members (
                userId TEXT NOT NULL PRIMARY KEY,
                crewId TEXT NOT NULL,
                displayName TEXT NOT NULL,
                level INTEGER NOT NULL,
                totalXp INTEGER NOT NULL,
                currentStreak INTEGER NOT NULL,
                updatedAtMillis INTEGER NOT NULL,
                syncState TEXT NOT NULL
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_crew_members_crewId ON crew_members(crewId)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS crew_feed (
                id TEXT NOT NULL PRIMARY KEY,
                crewId TEXT NOT NULL,
                authorId TEXT NOT NULL,
                authorName TEXT NOT NULL,
                questLogId TEXT NOT NULL,
                title TEXT NOT NULL,
                statType TEXT,
                difficulty TEXT,
                completedAtMillis INTEGER NOT NULL,
                approvedByCsv TEXT NOT NULL,
                syncState TEXT NOT NULL
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_crew_feed_crewId ON crew_feed(crewId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_crew_feed_completedAtMillis ON crew_feed(completedAtMillis)")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE quest_logs ADD COLUMN proofPhotoUrl TEXT")
        db.execSQL("ALTER TABLE quest_logs ADD COLUMN proofPhotoLocalPath TEXT")
        db.execSQL("ALTER TABLE crew_feed ADD COLUMN proofPhotoUrl TEXT")
    }
}