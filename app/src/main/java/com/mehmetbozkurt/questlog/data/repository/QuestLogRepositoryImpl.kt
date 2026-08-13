package com.mehmetbozkurt.questlog.data.repository

import com.mehmetbozkurt.questlog.core.common.IoDispatcher
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.core.sync.SyncScheduler
import com.mehmetbozkurt.questlog.data.mapper.toDomain
import com.mehmetbozkurt.questlog.data.mapper.toEntity
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.QuestLogRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestLogRepositoryImpl @Inject constructor(
    private val dao: QuestLogDao,
    private val authRepository: AuthRepository,
    private val syncScheduler: SyncScheduler,
    @IoDispatcher private val io: CoroutineDispatcher,
) : QuestLogRepository {

    override fun observeAll(): Flow<List<QuestLog>> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else dao.observeAll(user.uid).map { list -> list.map { it.toDomain() } }
        }

    override fun observeById(id: String): Flow<QuestLog?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun upsert(log: QuestLog) = withContext(io) {
        dao.upsert(log.toEntity())
        syncScheduler.requestSync()
    }

    override suspend fun setCompleted(id: String, completed: Boolean) = withContext(io) {
        dao.setCompleted(id, completed, System.currentTimeMillis())
        syncScheduler.requestSync()
    }

    override suspend fun delete(id: String) = withContext(io) {
        dao.softDelete(id, System.currentTimeMillis())
        syncScheduler.requestSync()
    }

    override fun newId(): String = UUID.randomUUID().toString()
}