package com.mehmetbozkurt.questlog.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.mehmetbozkurt.questlog.core.database.entity.CatalogCompletionEntity
import com.mehmetbozkurt.questlog.core.database.entity.CharacterEntity
import com.mehmetbozkurt.questlog.core.database.entity.FeatEntity
import com.mehmetbozkurt.questlog.core.database.entity.HabitSlotEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayQuestCompletionEntity
import com.mehmetbozkurt.questlog.core.database.entity.StatEntity
import com.mehmetbozkurt.questlog.core.database.entity.XpLedgerEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private fun userDoc(uid: String) = firestore.collection("users").document(uid)
    private fun stats(uid: String) = userDoc(uid).collection("stats")
    private fun feats(uid: String) = userDoc(uid).collection("feats")
    private fun ledger(uid: String) = userDoc(uid).collection("xpLedger")
    private fun completions(uid: String) = userDoc(uid).collection("questCompletions")
    private fun habitSlots(uid: String) = userDoc(uid).collection("habitSlots")
    private fun catalogCompletions(uid: String) = userDoc(uid).collection("catalogCompletions")

    suspend fun pushCharacter(entity: CharacterEntity) {
        userDoc(entity.userId).set(entity.toFireStoreMap(), SetOptions.merge()).await()
    }

    suspend fun pushStat(entity: StatEntity) {
        stats(entity.userId).document(entity.statType).set(entity.toFireStoreMap()).await()
    }

    suspend fun pushFeat(entity: FeatEntity) {
        feats(entity.userId).document(entity.id).set(entity.toFireStoreMap()).await()
    }

    suspend fun pushLedgerEntry(entity: XpLedgerEntity) {
        ledger(entity.userId).document(entity.id).set(entity.toFireStoreMap()).await()
    }

    suspend fun pushCompletion(entity: PathwayQuestCompletionEntity) {
        completions(entity.userId).document(entity.questId).set(entity.toFireStoreMap()).await()
    }

    suspend fun pushHabitSlot(entity: HabitSlotEntity) {
        habitSlots(entity.userId).document(entity.slotIndex.toString())
            .set(entity.toFireStoreMap()).await()
    }

    suspend fun pushCatalogCompletion(entity: CatalogCompletionEntity) {
        catalogCompletions(entity.userId).document(entity.taskId)
            .set(entity.toFireStoreMap()).await()
    }

    suspend fun deleteLedgerEntry(uid: String, docId: String) {
        ledger(uid).document(docId).delete().await()
    }

    suspend fun deleteUserDocument(uid: String) {
        listOf(stats(uid), feats(uid), ledger(uid), completions(uid), habitSlots(uid), catalogCompletions(uid))
            .forEach { collection ->
            collection.get().await().documents.forEach { it.reference.delete().await() }
        }
        userDoc(uid).delete().await()
    }

    suspend fun fetchCharacter(uid: String): CharacterEntity? =
        userDoc(uid).get(Source.SERVER).await().toCharacterEntityOrNull()

    suspend fun fetchStats(uid: String): List<StatEntity> =
        stats(uid).get().await().documents.mapNotNull { it.toStatEntityOrNull(uid) }

    suspend fun fetchFeats(uid: String): List<FeatEntity> =
        feats(uid).get().await().documents.mapNotNull { it.toFeatEntityOrNull(uid) }

    fun observeCharacter(uid: String): Flow<CharacterEntity?> = callbackFlow {
        val registration = userDoc(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.toCharacterEntityOrNull())
            }
        }
        awaitClose { registration.remove() }
    }

    fun observeStats(uid: String): Flow<List<StatEntity>> = callbackFlow {
        val registration = stats(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.documents.mapNotNull { it.toStatEntityOrNull(uid) })
            }
        }
        awaitClose { registration.remove() }
    }

    fun observeFeats(uid: String): Flow<List<FeatEntity>> = callbackFlow {
        val registration = feats(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.documents.mapNotNull { it.toFeatEntityOrNull(uid) })
            }
        }
        awaitClose { registration.remove() }
    }

    fun observeLedger(uid: String): Flow<List<XpLedgerEntity>> = callbackFlow {
        val registration = ledger(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.documents.mapNotNull { it.toLedgerEntityOrNull(uid) })
            }
        }
        awaitClose { registration.remove() }
    }

    fun observeHabitSlots(uid: String): Flow<List<HabitSlotEntity>> = callbackFlow {
        val registration = habitSlots(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.documents.mapNotNull { it.toHabitSlotEntityOrNull(uid) })
            }
        }
        awaitClose { registration.remove() }
    }

    fun observeCatalogCompletions(uid: String): Flow<List<CatalogCompletionEntity>> = callbackFlow {
        val registration = catalogCompletions(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.documents.mapNotNull { it.toCatalogCompletionEntityOrNull(uid) })
            }
        }
        awaitClose { registration.remove() }
    }

    fun observeCompletions(uid: String): Flow<List<PathwayQuestCompletionEntity>> = callbackFlow {
        val registration = completions(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.documents.mapNotNull { it.toCompletionEntityOrNull(uid) })
            }
        }
        awaitClose { registration.remove() }
    }
}
