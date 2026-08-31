package com.mehmetbozkurt.questlog.core.di

import android.content.Context
import androidx.room.Room
import com.mehmetbozkurt.questlog.core.database.MIGRATION_1_2
import com.mehmetbozkurt.questlog.core.database.MIGRATION_2_3
import com.mehmetbozkurt.questlog.core.database.MIGRATION_3_4
import com.mehmetbozkurt.questlog.core.database.MIGRATION_4_5
import com.mehmetbozkurt.questlog.core.database.MIGRATION_5_6
import com.mehmetbozkurt.questlog.core.database.MIGRATION_6_7
import com.mehmetbozkurt.questlog.core.database.MIGRATION_7_8
import com.mehmetbozkurt.questlog.core.database.MIGRATION_8_9
import com.mehmetbozkurt.questlog.core.database.MIGRATION_9_10
import com.mehmetbozkurt.questlog.core.database.MIGRATION_10_11
import com.mehmetbozkurt.questlog.core.database.MIGRATION_11_12
import com.mehmetbozkurt.questlog.core.database.MIGRATION_12_13
import com.mehmetbozkurt.questlog.core.database.QuestLogDatabase
import com.mehmetbozkurt.questlog.core.database.dao.CatalogDao
import com.mehmetbozkurt.questlog.core.database.dao.CharacterDao
import com.mehmetbozkurt.questlog.core.database.dao.CrewDao
import com.mehmetbozkurt.questlog.core.database.dao.PathwayDao
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuestLogDatabase = Room.databaseBuilder(
        context,
        QuestLogDatabase::class.java,
        "questlog.db"
    ).addMigrations(
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
    ).build()

    @Provides
    fun provideQuestLogDao(db: QuestLogDatabase): QuestLogDao = db.questLogDao()

    @Provides
    fun provideCharacterDao(db: QuestLogDatabase): CharacterDao = db.characterDao()

    @Provides
    fun providesPathwayDao(db: QuestLogDatabase): PathwayDao = db.pathwayDao()

    @Provides
    fun provideCrewDao(db: QuestLogDatabase): CrewDao = db.crewDao()

    @Provides
    fun provideCatalogDao(db: QuestLogDatabase): CatalogDao = db.catalogDao()
}