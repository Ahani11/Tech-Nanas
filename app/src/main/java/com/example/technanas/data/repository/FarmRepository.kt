package com.example.technanas.data.repository

import com.example.technanas.data.dao.FarmDao
import com.example.technanas.data.model.Farm
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FarmRepository(
    private val farmDao: FarmDao,
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val COLLECTION_FARMS = "farms"
    }

    // Room (cache) read
    fun getFarmsForUser(userId: Long): Flow<List<Farm>> =
        farmDao.getFarmsForUser(userId)

    suspend fun getFarmById(id: Long): Farm? =
        farmDao.getFarmById(id)

    // Firestore -> Room sync for a single user
    suspend fun refreshFromRemoteForUser(userId: Long, ownerEmail: String) {
        val snapshot = firestore.collection(COLLECTION_FARMS)
            .whereEqualTo("ownerEmail", ownerEmail)
            .get()
            .await()

        val farms = snapshot.documents.mapNotNull { doc ->
            try {
                val name = doc.getString("name") ?: return@mapNotNull null
                val size = doc.getString("size")
                val state = doc.getString("state")
                val address = doc.getString("address")
                val latitude = doc.getDouble("latitude")
                val longitude = doc.getDouble("longitude")

                Farm(
                    id = 0L, // Room will auto-generate ID
                    userId = userId,
                    name = name,
                    size = size,
                    state = state,
                    address = address,
                    latitude = latitude,
                    longitude = longitude,
                    remoteId = doc.id
                )
            } catch (_: Exception) {
                null
            }
        }

        farmDao.clearFarmsForUser(userId)
        if (farms.isNotEmpty()) {
            farmDao.insertAll(farms)
        }
    }

    // Create a new farm: Firestore first, then Room
    suspend fun addFarm(farm: Farm, ownerEmail: String) {
        val data = hashMapOf(
            "ownerEmail" to ownerEmail,
            "name" to farm.name,
            "size" to farm.size,
            "state" to farm.state,
            "address" to farm.address,
            "latitude" to farm.latitude,
            "longitude" to farm.longitude
        )

        val docRef = firestore.collection(COLLECTION_FARMS)
            .add(data)
            .await()

        val withRemoteId = farm.copy(remoteId = docRef.id)
        farmDao.insertFarm(withRemoteId)
    }

    // Update existing farm: Firestore then Room
    suspend fun updateFarm(farm: Farm, ownerEmail: String) {
        val remoteId = farm.remoteId
        val data = hashMapOf(
            "ownerEmail" to ownerEmail,
            "name" to farm.name,
            "size" to farm.size,
            "state" to farm.state,
            "address" to farm.address,
            "latitude" to farm.latitude,
            "longitude" to farm.longitude
        )

        if (remoteId == null) {
            // No remote id yet, treat as new farm
            addFarm(farm, ownerEmail)
            return
        }

        firestore.collection(COLLECTION_FARMS)
            .document(remoteId)
            .set(data)
            .await()

        farmDao.updateFarm(farm)
    }

    // Delete farm: Firestore (best effort) then Room
    suspend fun deleteFarm(farm: Farm) {
        val remoteId = farm.remoteId
        if (remoteId != null) {
            try {
                firestore.collection(COLLECTION_FARMS)
                    .document(remoteId)
                    .delete()
                    .await()
            } catch (_: Exception) {
                // ignore, still remove from local cache
            }
        }
        farmDao.deleteFarm(farm)
    }

    // Small internal helper: Task.await without extra dependency
    private suspend fun <T> Task<T>.await(): T =
        suspendCancellableCoroutine { cont ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cont.resume(task.result)
                } else {
                    cont.resumeWithException(
                        task.exception ?: Exception("Firestore task failed")
                    )
                }
            }
        }
}
