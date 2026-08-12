package com.mehmetbozkurt.questlog.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mehmetbozkurt.questlog.core.database.dao.CategoryDao
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.core.database.entity.CategoryEntity
import com.mehmetbozkurt.questlog.core.database.entity.QuestLogEntity

@Database(
    entities = [QuestLogEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class QuestLogDatabase : RoomDatabase() {
    abstract fun questLogDao(): QuestLogDao
    abstract fun categoryDao(): CategoryDao
}