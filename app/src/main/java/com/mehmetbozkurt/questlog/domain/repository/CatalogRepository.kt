package com.mehmetbozkurt.questlog.domain.repository

import com.mehmetbozkurt.questlog.domain.model.CatalogQuest
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    fun observeCatalog(): Flow<List<CatalogQuest>>
    suspend fun addToMyQuests(catalogQuest: CatalogQuest)
}