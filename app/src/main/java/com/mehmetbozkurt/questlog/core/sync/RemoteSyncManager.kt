package com.mehmetbozkurt.questlog.core.sync

import com.mehmetbozkurt.questlog.core.common.ApplicationScope
import com.mehmetbozkurt.questlog.core.database.dao.PathwayDao
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.data.remote.PathwayRemoteDataSource
import com.mehmetbozkurt.questlog.data.remote.QuestLogRemoteDataSource
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.PathwayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteSyncManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val remote: QuestLogRemoteDataSource,
    private val dao: QuestLogDao,
    private val pathwayRemote: PathwayRemoteDataSource,
    private val pathwayDao: PathwayDao,
    private val pathwayRepository: PathwayRepository,
    @ApplicationScope private val scope: CoroutineScope
) {
    fun start(){
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) emptyFlow()
            else remote.observeForUser(user.uid)
        }.catch { }//eklenecek
            .onEach { entities -> dao.mergeFromRemote(entities) }
            .launchIn(scope)

        scope.launch { pathwayRepository.refreshCatalog() }

        authRepository.currentUser
            .flatMapLatest { user ->
                if (user == null) emptyFlow()
                else pathwayRemote.observeProgressForUser(user.uid)
            }
            .catch { e -> android.util.Log.e("QuestLog", "Pathway sync", e) }
            .onEach { entities -> entities.forEach { pathwayDao.upsertProgress(it) } }
            .launchIn(scope)
    }
}