package com.mehmetbozkurt.questlog.core.database

import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        QuestLogDatabase::class.java,
    )

    private val allMigrations: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
        MIGRATION_9_10,
        MIGRATION_10_11,
        MIGRATION_11_12,
        MIGRATION_12_13,
    )

    private val latestVersion = LATEST_VERSION

    private fun migrateTo(name: String, target: Int = latestVersion): SupportSQLiteDatabase =
        helper.runMigrationsAndValidate(name, target, true, *allMigrations)

    private fun SupportSQLiteDatabase.longOf(sql: String): Long =
        query(sql).use { cursor ->
            assertTrue("no row for: $sql", cursor.moveToFirst())
            cursor.getLong(0)
        }

    private fun SupportSQLiteDatabase.stringOf(sql: String): String? =
        query(sql).use { cursor ->
            assertTrue("no row for: $sql", cursor.moveToFirst())
            if (cursor.isNull(0)) null else cursor.getString(0)
        }

    private fun SupportSQLiteDatabase.columnsOf(table: String): Set<String> =
        query("PRAGMA table_info($table)").use { cursor ->
            buildSet {
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) add(cursor.getString(nameIndex))
            }
        }

    @Test
    fun everyMigrationIsRegistered() {
        assertEquals(latestVersion - 1, allMigrations.size)
        allMigrations.forEachIndexed { index, migration ->
            assertEquals(index + 1, migration.startVersion)
            assertEquals(index + 2, migration.endVersion)
        }
    }

    @Test
    fun theExportedSchemasKeepUpWithTheDatabaseVersion() {
        val exported = InstrumentationRegistry.getInstrumentation().context.assets
            .list(SCHEMA_DIR)
            .orEmpty()
            .mapNotNull { it.removeSuffix(".json").toIntOrNull() }

        assertEquals(
            "exported schemas are $exported but the test targets $latestVersion",
            latestVersion,
            exported.maxOrNull(),
        )
        assertEquals((1..latestVersion).toSet(), exported.toSet())
    }

    @Test
    fun migratesFromTheVeryFirstVersion() {
        helper.createDatabase("chain-from-1", 1).close()
        migrateTo("chain-from-1").close()
    }

    @Test
    fun everyShippedVersionCanReachTheLatest() {
        for (start in 1 until latestVersion) {
            val name = "chain-from-$start"
            InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(name)
            helper.createDatabase(name, start).use { db ->
                assertEquals(
                    "database for $start was not created at that version",
                    start.toLong(),
                    db.longOf("PRAGMA user_version"),
                )
            }
            try {
                migrateTo(name).close()
            } catch (e: Throwable) {
                throw AssertionError("migration chain broke starting from version $start", e)
            }
        }
    }

    @Test
    fun questLogsSurviveTheFirstMigration() {
        helper.createDatabase("v1-data", 1).use { db ->
            db.execSQL(
                """
                INSERT INTO quest_logs
                    (id, ownerId, type, title, description, isCompleted,
                     createdAtMillis, updatedAtMillis, isDeleted, syncState)
                VALUES
                    ('log-1', 'user-1', 'QUEST', 'Eski gorev', '', 0, 100, 100, 0, 'SYNCED')
                """.trimIndent(),
            )
        }

        migrateTo("v1-data", 2).use { db ->
            assertEquals(1L, db.longOf("SELECT COUNT(*) FROM quest_logs WHERE id = 'log-1'"))
            assertEquals(
                "NONE",
                db.stringOf("SELECT proofLevel FROM quest_logs WHERE id = 'log-1'"),
            )
            assertNull(db.stringOf("SELECT statType FROM quest_logs WHERE id = 'log-1'"))
        }
    }

    @Test
    fun earnedProgressSurvivesTheHabitSlotRewrite() {
        helper.createDatabase("v10-data", 10).use { db ->
            db.execSQL(
                """
                INSERT INTO characters
                    (userId, totalXp, pendingFeatChoices, createdAtMillis, updatedAtMillis,
                     syncState, approvalDayMillis, approvalsToday)
                VALUES ('user-1', 5275, 2, 100, 200, 'SYNCED', 0, 0)
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO stats
                    (userId, statType, value, currentXp, updatedAtMillis, syncState)
                VALUES ('user-1', 'INT', 14, 120, 200, 'SYNCED')
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO xp_ledger
                    (id, userId, logId, statType, baseXp, finalXp, earnedAtMillis, syncState)
                VALUES ('led-1', 'user-1', 'log-1', 'INT', 25, 33, 150, 'SYNCED')
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO quest_logs
                    (id, ownerId, type, title, description, isCompleted, createdAtMillis,
                     updatedAtMillis, isDeleted, syncState, proofLevel, statType, difficulty)
                VALUES ('log-1', 'user-1', 'QUEST', 'Serbest gorev', '', 1, 100, 200, 0,
                        'SYNCED', 'NOTE', 'INT', 'MEDIUM')
                """.trimIndent(),
            )
        }

        migrateTo("v10-data").use { db ->
            assertEquals(5275L, db.longOf("SELECT totalXp FROM characters WHERE userId = 'user-1'"))
            assertEquals(2L, db.longOf("SELECT pendingFeatChoices FROM characters WHERE userId = 'user-1'"))
            assertEquals(14L, db.longOf("SELECT value FROM stats WHERE statType = 'INT'"))
            assertEquals(120L, db.longOf("SELECT currentXp FROM stats WHERE statType = 'INT'"))
            assertEquals(33L, db.longOf("SELECT finalXp FROM xp_ledger WHERE id = 'led-1'"))
            assertEquals(1L, db.longOf("SELECT COUNT(*) FROM quest_logs WHERE id = 'log-1'"))
            assertNull(db.stringOf("SELECT slotIndex FROM quest_logs WHERE id = 'log-1'"))
            assertEquals(0L, db.longOf("SELECT COUNT(*) FROM habit_slots"))
        }
    }

    @Test
    fun crewMembersKeepTheirCardWhenAvatarsArrive() {
        helper.createDatabase("v12-data", 12).use { db ->
            db.execSQL(
                """
                INSERT INTO crew_members
                    (userId, crewId, displayName, level, totalXp, currentStreak,
                     updatedAtMillis, syncState)
                VALUES ('user-2', 'crew-1', 'Yoldas', 7, 4200, 12, 300, 'SYNCED')
                """.trimIndent(),
            )
        }

        migrateTo("v12-data").use { db ->
            assertEquals("Yoldas", db.stringOf("SELECT displayName FROM crew_members WHERE userId = 'user-2'"))
            assertEquals(7L, db.longOf("SELECT level FROM crew_members WHERE userId = 'user-2'"))
            assertEquals(12L, db.longOf("SELECT currentStreak FROM crew_members WHERE userId = 'user-2'"))
            assertNull(db.stringOf("SELECT photoUrl FROM crew_members WHERE userId = 'user-2'"))
        }
    }

    @Test
    fun theCatalogTablesArriveEmptyAndReady() {
        helper.createDatabase("v11-data", 11).close()

        migrateTo("v11-data").use { db ->
            assertEquals(0L, db.longOf("SELECT COUNT(*) FROM catalog_tasks"))
            assertEquals(0L, db.longOf("SELECT COUNT(*) FROM catalog_completions"))
            assertTrue(
                db.columnsOf("catalog_tasks").containsAll(
                    listOf("id", "title", "description", "titleEn", "descriptionEn", "statType", "difficulty", "sortOrder"),
                ),
            )
            assertTrue(
                db.columnsOf("catalog_completions").containsAll(
                    listOf("userId", "taskId", "completions", "lastCompletedAtMillis", "syncState"),
                ),
            )
        }
    }

    @Test
    fun tablesRetiredInVersionSixStayGone() {
        helper.createDatabase("v5-data", 5).close()

        migrateTo("v5-data").use { db ->
            val tables = db.query(
                "SELECT name FROM sqlite_master WHERE type = 'table'",
            ).use { cursor ->
                buildSet { while (cursor.moveToNext()) add(cursor.getString(0)) }
            }
            assertTrue("categories was not dropped", "categories" !in tables)
            assertTrue("catalog_quests was not dropped", "catalog_quests" !in tables)
        }
    }

    private companion object {
        const val LATEST_VERSION = 13
        const val SCHEMA_DIR = "com.mehmetbozkurt.questlog.core.database.QuestLogDatabase"
    }
}
