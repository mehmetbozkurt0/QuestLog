package com.mehmetbozkurt.questlog.core.di

import android.content.Context
import androidx.room.Room
import com.mehmetbozkurt.questlog.core.database.MIGRATION_1_2
import com.mehmetbozkurt.questlog.core.database.QuestLogDatabase
import com.mehmetbozkurt.questlog.core.database.dao.CategoryDao
import com.mehmetbozkurt.questlog.core.database.dao.CharacterDao
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
    ).addMigrations(MIGRATION_1_2).build()

    @Provides
    fun provideQuestLogDao(db: QuestLogDatabase): QuestLogDao = db.questLogDao()

    @Provides
    fun provideCategoryDao(db: QuestLogDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideCharacterDao(db: QuestLogDatabase): CharacterDao = db.characterDao()
}