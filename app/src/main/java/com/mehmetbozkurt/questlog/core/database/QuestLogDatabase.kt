package com.mehmetbozkurt.questlog.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mehmetbozkurt.questlog.core.database.dao.CatalogDao
import com.mehmetbozkurt.questlog.core.database.dao.CharacterDao
import com.mehmetbozkurt.questlog.core.database.dao.CrewDao
import com.mehmetbozkurt.questlog.core.database.dao.PathwayDao
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.core.database.entity.CatalogCompletionEntity
import com.mehmetbozkurt.questlog.core.database.entity.CatalogTaskEntity
import com.mehmetbozkurt.questlog.core.database.entity.CharacterEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewFeedEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewMemberEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewMessageEntity
import com.mehmetbozkurt.questlog.core.database.entity.FeatEntity
import com.mehmetbozkurt.questlog.core.database.entity.HabitSlotEntity
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
        CrewEntity::class,
        CrewMemberEntity::class,
        CrewFeedEntity::class,
        CrewMessageEntity::class,
        HabitSlotEntity::class,
        CatalogTaskEntity::class,
        CatalogCompletionEntity::class,
    ],
    version = 14,
    exportSchema = true,
)
abstract class QuestLogDatabase : RoomDatabase() {
    abstract fun questLogDao(): QuestLogDao
    abstract fun characterDao(): CharacterDao
    abstract fun pathwayDao(): PathwayDao
    abstract fun crewDao(): CrewDao
    abstract fun catalogDao(): CatalogDao
}
