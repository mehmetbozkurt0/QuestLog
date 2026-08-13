package com.mehmetbozkurt.questlog.core.sync

import com.mehmetbozkurt.questlog.core.common.ApplicationScope
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
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
    @ApplicationScope private val scope: CoroutineScope
) {
    fun start(){
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) emptyFlow()
            else remote.observeForUser(user.uid)
        }.catch { }//eklenecek
            .onEach { entities -> dao.mergeFromRemote(entities) }
            .launchIn(scope)
    }
}