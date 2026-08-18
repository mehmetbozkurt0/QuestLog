package com.mehmetbozkurt.questlog.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.mehmetbozkurt.questlog.core.database.entity.PathwayEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayProgressEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayQuestEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class PathwayCatalogSnapshot(
    val pathways: List<PathwayEntity>,
    val quests: List<PathwayQuestEntity>,
)

@Singleton
class PathwayRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val pathwaysCollection get() = firestore.collection("pathways")
    private val progressCollection get() = firestore.collection("pathwayProgress")

    /** Tüm pathway kataloğunu tek seferde çeker (alt koleksiyonlarla birlikte). */
    suspend fun fetchCatalog(): PathwayCatalogSnapshot {
        val pathwayDocs = pathwaysCollection.get().await()
        val pathways = pathwayDocs.documents.mapNotNull { it.toPathwayEntityOrNull() }

        val quests = mutableListOf<PathwayQuestEntity>()
        pathwayDocs.documents.forEach { doc ->
            val questDocs = doc.reference.collection("quests").get().await()
            quests += questDocs.documents.mapNotNull {
                it.toPathwayQuestEntityOrNull(doc.id)
            }
        }

        return PathwayCatalogSnapshot(pathways, quests)
    }

    suspend fun pushProgress(entity: PathwayProgressEntity) {
        progressCollection
            .document("${entity.userId}_${entity.pathwayId}")
            .set(entity.toFireStoreMap())
            .await()
    }

    fun observeProgressForUser(uid: String): Flow<List<PathwayProgressEntity>> = callbackFlow {
        val registration = progressCollection
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(
                        snapshot.documents.mapNotNull { doc ->
                            val pathwayId = doc.getString("pathwayId") ?: return@mapNotNull null
                            PathwayProgressEntity(
                                userId = uid,
                                pathwayId = pathwayId,
                                startedAtMillis = doc.getLong("startedAtMillis") ?: 0L,
                                lastActivityAtMillis = doc.getLong("lastActivityAtMillis") ?: 0L,
                                escrowedXp = (doc.getLong("escrowedXp") ?: 0L).toInt(),
                                completedAtMillis = doc.getLong("completedAtMillis"),
                                abandonedAtMillis = doc.getLong("abandonedAtMillis"),
                                syncState = com.mehmetbozkurt.questlog.core.database.entity.SyncState.SYNCED.name,
                            )
                        }
                    )
                }
            }
        awaitClose { registration.remove() }
    }
}