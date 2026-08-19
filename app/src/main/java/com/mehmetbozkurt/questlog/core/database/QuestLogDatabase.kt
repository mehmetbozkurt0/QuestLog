package com.mehmetbozkurt.questlog.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mehmetbozkurt.questlog.core.database.dao.CharacterDao
import com.mehmetbozkurt.questlog.core.database.dao.PathwayDao
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.core.database.entity.CharacterEntity
import com.mehmetbozkurt.questlog.core.database.entity.FeatEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayEntity
import com.mehmetbozkurt.questlog.core.database.entity.PendingDeletionEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayProgressEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayQuestCompletionEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayQuestEntity
import com.mehmetbozkurt.questlog.core.database.entity.QuestLogEntity
import com.mehmetbozkurt.questlog.core.database.entity.StatEntity
import com.mehmetbozkurt.questlog.core.database.entity.XpLedgerEntity

@Database(
    entities = [
        QuestLogEntity::class,
        CharacterEntity::class,
        StatEntity::class,
        FeatEntity::class,
        XpLedgerEntity::class,
        PathwayEntity::class,
        PathwayQuestEntity::class,
        PathwayProgressEntity::class,
        PathwayQuestCompletionEntity::class,
        PendingDeletionEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
abstract class QuestLogDatabase : RoomDatabase() {
    abstract fun questLogDao(): QuestLogDao
    abstract fun characterDao(): CharacterDao
    abstract fun pathwayDao(): PathwayDao
}