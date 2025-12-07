package com.example.technanas.data.repository

import com.example.technanas.data.dao.AnnouncementDao
import com.example.technanas.data.model.Announcement
import com.example.technanas.data.model.AnnouncementType
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AnnouncementRepository(
    private val announcementDao: AnnouncementDao,
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val COLLECTION_ANNOUNCEMENTS = "announcements"
    }

    // ---------------- Room (cache) read ----------------

    fun getAllAnnouncements(): Flow<List<Announcement>> = announcementDao.getAll()

    suspend fun getAnnouncementById(id: Long): Announcement? =
        announcementDao.getById(id)

    suspend fun getCount(): Int = announcementDao.getCount()

    // ---------------- Firestore + Room sync ----------------

    /**
     * Download all announcements from Firestore and replace the local Room cache.
     */
    suspend fun refreshFromRemote() {
        val snapshot = firestore.collection(COLLECTION_ANNOUNCEMENTS)
            .orderBy("dateMillis", Query.Direction.DESCENDING)
            .get()
            .await()

        val list = snapshot.documents.mapNotNull { doc ->
            parseAnnouncementDocument(doc)
        }

        announcementDao.clearAll()
        if (list.isNotEmpty()) {
            announcementDao.insertAll(list)
        }
    }

    /**
     * Get announcements that belong to one owner (for "My announcements" screen).
     */
    suspend fun getAnnouncementsForOwner(ownerEmail: String): List<Announcement> {
        val snapshot = firestore.collection(COLLECTION_ANNOUNCEMENTS)
            .whereEqualTo("ownerEmail", ownerEmail)
            .get()
            .await()

        return snapshot.documents
            .mapNotNull { doc -> parseAnnouncementDocument(doc) }
            .sortedByDescending { it.dateMillis }
    }

    /**
     * Create a new announcement:
     *  - writes to Firestore (source of truth)
     *  - then inserts into Room cache
     *
     * ownerEmail is stored only in Firestore and used to filter "My announcements".
     */
    suspend fun insertAnnouncement(
        announcement: Announcement,
        ownerEmail: String
    ) {
        val data = hashMapOf(
            "title" to announcement.title,
            "shortDescription" to announcement.shortDescription,
            "fullDescription" to announcement.fullDescription,
            "type" to announcement.type.name,
            "dateMillis" to announcement.dateMillis,
            "externalUrl" to announcement.externalUrl,
            "ownerEmail" to ownerEmail
        )

        firestore.collection(COLLECTION_ANNOUNCEMENTS)
            .add(data)
            .await()

        announcementDao.insert(announcement)
    }

    suspend fun updateAnnouncement(announcement: Announcement) =
        announcementDao.update(announcement)

    suspend fun deleteAnnouncement(announcement: Announcement) =
        announcementDao.delete(announcement)

    // ---------- New helper: find owner email for a clicked announcement ----------

    /**
     * Try to find the ownerEmail in Firestore for a given announcement.
     * We match on title + dateMillis (good enough for a uni project).
     */
    suspend fun findOwnerEmailFor(announcement: Announcement): String? {
        val snapshot = firestore.collection(COLLECTION_ANNOUNCEMENTS)
            .whereEqualTo("title", announcement.title)
            .whereEqualTo("dateMillis", announcement.dateMillis)
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) return null
        return snapshot.documents.first().getString("ownerEmail")
    }

    // ---------- Internal helpers ----------

    private fun parseAnnouncementDocument(doc: DocumentSnapshot): Announcement? {
        return try {
            val title = doc.getString("title") ?: return null
            val shortDescription = doc.getString("shortDescription") ?: ""
            val fullDescription = doc.getString("fullDescription") ?: ""
            val typeStr = doc.getString("type") ?: "GENERAL"
            val type = try {
                AnnouncementType.valueOf(typeStr)
            } catch (_: Exception) {
                AnnouncementType.GENERAL
            }
            val dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis()
            val externalUrl = doc.getString("externalUrl")

            Announcement(
                id = 0L,
                title = title,
                shortDescription = shortDescription,
                fullDescription = fullDescription,
                type = type,
                dateMillis = dateMillis,
                externalUrl = externalUrl
            )
        } catch (_: Exception) {
            null
        }
    }

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
