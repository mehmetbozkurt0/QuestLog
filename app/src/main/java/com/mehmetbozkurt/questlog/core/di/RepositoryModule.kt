package com.mehmetbozkurt.questlog.core.di

import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.data.repository.AuthRepositoryImpl
import com.mehmetbozkurt.questlog.data.repository.CategoryRepositoryImpl
import com.mehmetbozkurt.questlog.data.repository.QuestLogRepositoryImpl
import com.mehmetbozkurt.questlog.domain.repository.CategoryRepository
import com.mehmetbozkurt.questlog.domain.repository.QuestLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindQuestLogRepository(impl: QuestLogRepositoryImpl): QuestLogRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
}