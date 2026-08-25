package com.mehmetbozkurt.questlog.core.di

import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.data.repository.AccountRepositoryImpl
import com.mehmetbozkurt.questlog.data.repository.AuthRepositoryImpl
import com.mehmetbozkurt.questlog.data.repository.CharacterRepositoryImpl
import com.mehmetbozkurt.questlog.domain.repository.AccountRepository
import com.mehmetbozkurt.questlog.data.repository.CrewRepositoryImpl
import com.mehmetbozkurt.questlog.data.repository.CatalogRepositoryImpl
import com.mehmetbozkurt.questlog.data.repository.PathwayRepositoryImpl
import com.mehmetbozkurt.questlog.data.repository.QuestLogRepositoryImpl
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.CrewRepository
import com.mehmetbozkurt.questlog.domain.repository.CatalogRepository
import com.mehmetbozkurt.questlog.domain.repository.PathwayRepository
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
    abstract fun bindCharacterRepository(impl: CharacterRepositoryImpl): CharacterRepository

    @Binds
    @Singleton
    abstract fun bindPathwayRepository(impl: PathwayRepositoryImpl): PathwayRepository

    @Binds
    abstract fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository

    @Binds
    @Singleton
    abstract fun bindCrewRepository(impl: CrewRepositoryImpl): CrewRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository
}