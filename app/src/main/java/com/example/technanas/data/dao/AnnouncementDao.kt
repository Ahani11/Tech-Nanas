package com.example.technanas.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.technanas.data.model.Announcement
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {

    @Query("SELECT * FROM announcements ORDER BY dateMillis DESC")
    fun getAll(): Flow<List<Announcement>>

    @Query("SELECT * FROM announcements WHERE id = :id")
    suspend fun getById(id: Long): Announcement?

    // single insert (used when adding locally)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(announcement: Announcement): Long

    // bulk insert for sync (Firestore -> Room)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(announcements: List<Announcement>)

    @Update
    suspend fun update(announcement: Announcement)

    @Delete
    suspend fun delete(announcement: Announcement)

    @Query("DELETE FROM announcements")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM announcements")
    suspend fun getCount(): Int
}
