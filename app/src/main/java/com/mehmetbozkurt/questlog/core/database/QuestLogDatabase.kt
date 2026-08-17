package com.mehmetbozkurt.questlog.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mehmetbozkurt.questlog.core.database.dao.CatalogDao
import com.mehmetbozkurt.questlog.core.database.dao.CategoryDao
import com.mehmetbozkurt.questlog.core.database.dao.CharacterDao
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.core.database.entity.CatalogQuestEntity
import com.mehmetbozkurt.questlog.core.database.entity.CategoryEntity
import com.mehmetbozkurt.questlog.core.database.entity.CharacterEntity
import com.mehmetbozkurt.questlog.core.database.entity.FeatEntity
import com.mehmetbozkurt.questlog.core.database.entity.QuestLogEntity
import com.mehmetbozkurt.questlog.core.database.entity.StatEntity
import com.mehmetbozkurt.questlog.core.database.entity.XpLedgerEntity

@Database(
    entities = [
        QuestLogEntity::class,
        CategoryEntity::class,
        CharacterEntity::class,
        StatEntity::class,
        FeatEntity::class,
        XpLedgerEntity::class,
        CatalogQuestEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class QuestLogDatabase : RoomDatabase() {
    abstract fun questLogDao(): QuestLogDao
    abstract fun categoryDao(): CategoryDao
    abstract fun characterDao(): CharacterDao
    abstract fun catalogDao(): CatalogDao
}