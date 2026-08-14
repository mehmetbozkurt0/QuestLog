package com.mehmetbozkurt.questlog.data.repository

import com.mehmetbozkurt.questlog.core.common.IoDispatcher
import com.mehmetbozkurt.questlog.core.database.dao.CategoryDao
import com.mehmetbozkurt.questlog.core.sync.SyncScheduler
import com.mehmetbozkurt.questlog.data.mapper.toDomain
import com.mehmetbozkurt.questlog.data.mapper.toEntity
import com.mehmetbozkurt.questlog.domain.model.Category
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
    private val authRepository: AuthRepository,
    private val syncScheduler: SyncScheduler,
    @IoDispatcher private val io: CoroutineDispatcher,
) : CategoryRepository {

    override fun observeAll(): Flow<List<Category>> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else dao.observeAll(user.uid).map { list -> list.map { it.toDomain() } }
        }

    override suspend fun create(name: String, colorHex: String) = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext
        dao.upsert(
            Category(
                id = UUID.randomUUID().toString(),
                ownerId = user.uid,
                name = name.trim(),
                colorHex = colorHex,
                createdAt = Instant.now(),
            ).toEntity()
        )
        syncScheduler.requestSync()
    }

    override suspend fun delete(id: String) = withContext(io) {
        dao.softDelete(id)
        syncScheduler.requestSync()
    }

    override suspend fun ensureDefaults() = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext
        if (dao.count(user.uid) > 0) return@withContext

        DEFAULT_CATEGORIES.forEach { (name, color) ->
            dao.upsert(
                Category(
                    id = UUID.randomUUID().toString(),
                    ownerId = user.uid,
                    name = name,
                    colorHex = color,
                    createdAt = Instant.now(),
                ).toEntity()
            )
        }
        syncScheduler.requestSync()
    }

    companion object {
        private val DEFAULT_CATEGORIES = listOf(
            "Ana Görev" to "#C8A951",
            "Yan Görev" to "#7B5EA7",
            "Hazırlık" to "#5B8FA8",
            "Kişisel" to "#8C5A3C",
        )
    }
}