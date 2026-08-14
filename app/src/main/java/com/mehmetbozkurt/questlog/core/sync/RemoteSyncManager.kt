package com.mehmetbozkurt.questlog.core.sync

import com.mehmetbozkurt.questlog.core.common.ApplicationScope
import com.mehmetbozkurt.questlog.core.database.dao.CategoryDao
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.data.remote.CategoryRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.QuestLogRemoteDataSource
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteSyncManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val remote: QuestLogRemoteDataSource,
    private val dao: QuestLogDao,
    private val categoryRemote: CategoryRemoteDataSource,
    private val categoryDao: CategoryDao,
    @ApplicationScope private val scope: CoroutineScope
) {
    fun start(){
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) emptyFlow()
            else remote.observeForUser(user.uid)
        }.catch { }//eklenecek
            .onEach { entities -> dao.mergeFromRemote(entities) }
            .launchIn(scope)

        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) emptyFlow() else  categoryRemote.observeForUser(user.uid)
        }.catch { e ->
            android.util.Log.e("QuestLog", "Category sync")
        }.onEach {entities ->
            categoryDao.mergeFromRemote(entities)
        }.launchIn(scope)
    }
}