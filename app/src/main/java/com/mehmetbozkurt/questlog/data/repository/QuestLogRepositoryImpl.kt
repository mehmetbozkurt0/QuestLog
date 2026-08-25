package com.mehmetbozkurt.questlog.data.repository

import com.mehmetbozkurt.questlog.core.common.IoDispatcher
import com.mehmetbozkurt.questlog.core.common.startOfTodayMillis
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.core.database.entity.HabitSlotEntity
import com.mehmetbozkurt.questlog.core.database.entity.SyncState
import com.mehmetbozkurt.questlog.core.notification.ReminderScheduler
import com.mehmetbozkurt.questlog.core.sync.SyncScheduler
import com.mehmetbozkurt.questlog.data.mapper.toDomain
import com.mehmetbozkurt.questlog.data.mapper.toEntity
import com.mehmetbozkurt.questlog.domain.model.HabitSlot
import com.mehmetbozkurt.questlog.domain.model.ProofLevel
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import com.mehmetbozkurt.questlog.domain.repository.QuestLogRepository
import com.mehmetbozkurt.questlog.domain.progression.HabitRules
import com.mehmetbozkurt.questlog.domain.repository.XpAward
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    private val characterRepository: CharacterRepository,
    private val reminderScheduler: ReminderScheduler,
    @IoDispatcher private val io: CoroutineDispatcher,
) : QuestLogRepository {

    override fun observeAll(): Flow<List<QuestLog>> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else dao.observeAll(user.uid).map { list -> list.map { it.toDomain() } }
        }

    override fun observeHabitSlots(): Flow<List<HabitSlot>> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) {
                flowOf(emptySlots())
            } else {
                combine(
                    dao.observeHabits(user.uid),
                    dao.observeSlots(user.uid),
                ) { habits, slots ->
                    val todayStart = startOfTodayMillis()
                    val questBySlot = habits.associateBy { it.slotIndex }
                    val stateBySlot = slots.associateBy { it.slotIndex }
                    HabitRules.slotRange.map { index ->
                        HabitSlot(
                            index = index,
                            quest = questBySlot[index]?.toDomain(),
                            burnedToday = HabitRules.isBurnedToday(
                                stateBySlot[index]?.lastCompletedDayMillis ?: 0L,
                                todayStart,
                            ),
                        )
                    }
                }
            }
        }

    override suspend fun clearHabitSlot(slotIndex: Int) = withContext(io) {
        val user = authRepository.currentUserSync() ?: return@withContext
        if (!HabitRules.isValidSlot(slotIndex)) return@withContext

        dao.getHabitInSlot(user.uid, slotIndex)?.let { habit ->
            dao.softDelete(habit.id, System.currentTimeMillis())
            reminderScheduler.cancelQuestReminder(habit.id)
        }
        dao.clearSlotAssignment(user.uid, slotIndex, System.currentTimeMillis())
        syncScheduler.requestSync()
    }

    private fun emptySlots(): List<HabitSlot> =
        HabitRules.slotRange.map { HabitSlot(it, null, false) }

    override fun observeById(id: String): Flow<QuestLog?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun upsert(log: QuestLog) = withContext(io) {
        dao.upsert(log.toEntity())
        syncScheduler.requestSync()

        val remindAt = log.remindAt?.toEpochMilli()
        if (remindAt != null && !log.isCompleted) {
            reminderScheduler.scheduleQuestReminder(log.id, log.title, remindAt)
        } else {
            reminderScheduler.cancelQuestReminder(log.id)
        }
    }

    override suspend fun setCompleted(id: String, completed: Boolean) = withContext(io) {
        withContext(io) {
            val now = System.currentTimeMillis()
            dao.setCompleted(id, completed, now)
            dao.setCompletedAt(id, if (completed) now else null)
            syncScheduler.requestSync()

            val log = dao.getById(id)?.toDomain() ?: return@withContext null

            if (completed) {
                reminderScheduler.cancelQuestReminder(id)
            } else {
                log.remindAt?.toEpochMilli()?.let { at ->
                    reminderScheduler.scheduleQuestReminder(id, log.title, at)
                }
            }

            val slotIndex = log.slotIndex
            if (!completed) {
                characterRepository.revokeXpFor(
                    logId = id,
                    sinceMillis = if (slotIndex != null) startOfTodayMillis() else null,
                )
                if (slotIndex != null) unburnSlotForToday(slotIndex)
                return@withContext null
            }

            if (slotIndex != null && isSlotBurnedToday(slotIndex)) {
                return@withContext XpAward.Rejected(XpAward.RejectReason.ALREADY_AWARDED_TODAY)
            }

            val award = characterRepository.awardXpFor(log)
            if (slotIndex != null && award is XpAward.Granted) burnSlotForToday(slotIndex)
            award
        }
    }

    override suspend fun completeWithProof(
        id: String,
        note: String?,
        photoLocalPath: String?,
    ): XpAward? = withContext(io) {
        val level = when {
            photoLocalPath != null -> ProofLevel.PHOTO
            !note.isNullOrBlank() -> ProofLevel.NOTE
            else -> ProofLevel.NONE
        }
        dao.setProof(
            id = id,
            level = level.name,
            note = note?.takeIf { it.isNotBlank() },
            photoLocalPath = photoLocalPath,
            nowMillis = System.currentTimeMillis(),
        )
        setCompleted(id, true)
    }

    override suspend fun delete(id: String) = withContext(io) {
        dao.softDelete(id, System.currentTimeMillis())
        reminderScheduler.cancelQuestReminder(id)
        syncScheduler.requestSync()
    }


    private suspend fun isSlotBurnedToday(slotIndex: Int): Boolean {
        val user = authRepository.currentUserSync() ?: return false
        val slot = dao.getSlot(user.uid, slotIndex) ?: return false
        return HabitRules.isBurnedToday(slot.lastCompletedDayMillis, startOfTodayMillis())
    }

    private suspend fun burnSlotForToday(slotIndex: Int) {
        val user = authRepository.currentUserSync() ?: return
        dao.upsertSlot(
            HabitSlotEntity(
                userId = user.uid,
                slotIndex = slotIndex,
                lastCompletedDayMillis = startOfTodayMillis(),
                updatedAtMillis = System.currentTimeMillis(),
            )
        )
        syncScheduler.requestSync()
    }

    private suspend fun unburnSlotForToday(slotIndex: Int) {
        val user = authRepository.currentUserSync() ?: return
        val slot = dao.getSlot(user.uid, slotIndex) ?: return
        if (!HabitRules.isBurnedToday(slot.lastCompletedDayMillis, startOfTodayMillis())) return
        dao.upsertSlot(
            slot.copy(
                lastCompletedDayMillis = 0L,
                updatedAtMillis = System.currentTimeMillis(),
                syncState = SyncState.PENDING.name,
            )
        )
        syncScheduler.requestSync()
    }

    override fun newId(): String = UUID.randomUUID().toString()
}