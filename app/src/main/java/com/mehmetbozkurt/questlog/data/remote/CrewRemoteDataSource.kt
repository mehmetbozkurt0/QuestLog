package com.mehmetbozkurt.questlog.data.remote

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mehmetbozkurt.questlog.core.database.entity.CrewEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewFeedEntity
import com.mehmetbozkurt.questlog.core.database.entity.CrewMemberEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrewRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private fun crewDoc(crewId: String) = firestore.collection("crews").document(crewId)
    private fun members(crewId: String) = crewDoc(crewId).collection("members")
    private fun feed(crewId: String) = crewDoc(crewId).collection("feed")
    private fun codeDoc(code: String) = firestore.collection("inviteCodes").document(code)

    suspend fun createCrew(entity: CrewEntity) {
        codeDoc(entity.inviteCode).set(mapOf("crewId" to entity.crewId)).await()
        crewDoc(entity.crewId).set(entity.toFireStoreMap()).await()
    }

    suspend fun findCrewIdByCode(code: String): String? =
        codeDoc(code).get().await().getString("crewId")

    suspend fun fetchCrew(crewId: String): CrewEntity? =
        crewDoc(crewId).get().await().toCrewEntityOrNull()

    suspend fun joinCrew(crewId: String, uid: String) {
        crewDoc(crewId).update("memberIds", FieldValue.arrayUnion(uid)).await()
    }

    suspend fun leaveCrew(crewId: String, uid: String) {
        crewDoc(crewId).update("memberIds", FieldValue.arrayRemove(uid)).await()
        members(crewId).document(uid).delete().await()
    }

    suspend fun deleteFeedEntriesBy(crewId: String, uid: String) {
        feed(crewId).whereEqualTo("authorId", uid).get().await().documents
            .forEach { it.reference.delete().await() }
    }

    suspend fun pushMemberCard(entity: CrewMemberEntity) {
        members(entity.crewId).document(entity.userId).set(entity.toFireStoreMap()).await()
    }

    suspend fun pushFeedEntry(entity: CrewFeedEntity) {
        feed(entity.crewId).document(entity.id).set(entity.toFireStoreMap()).await()
    }

    suspend fun approveFeedEntry(crewId: String, entryId: String, uid: String) {
        feed(crewId).document(entryId).update("approvedBy", FieldValue.arrayUnion(uid)).await()
    }

    fun observeCrew(crewId: String): Flow<CrewEntity?> = callbackFlow {
        val registration = crewDoc(crewId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.toCrewEntityOrNull())
            }
        }
        awaitClose { registration.remove() }
    }

    fun observeMembers(crewId: String): Flow<List<CrewMemberEntity>> = callbackFlow {
        val registration = members(crewId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.documents.mapNotNull { it.toCrewMemberEntityOrNull(crewId) })
            }
        }
        awaitClose { registration.remove() }
    }

    fun observeFeed(crewId: String): Flow<List<CrewFeedEntity>> = callbackFlow {
        val registration = feed(crewId)
            .orderBy("completedAtMillis", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(FEED_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toCrewFeedEntityOrNull(crewId) })
                }
            }
        awaitClose { registration.remove() }
    }

    companion object {
        const val FEED_LIMIT = 50L
    }
}
