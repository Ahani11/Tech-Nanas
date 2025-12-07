package com.example.technanas.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.technanas.data.model.Farm
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {

    @Query("SELECT * FROM farms WHERE userId = :userId ORDER BY name")
    fun getFarmsForUser(userId: Long): Flow<List<Farm>>

    @Query("SELECT * FROM farms WHERE id = :id")
    suspend fun getFarmById(id: Long): Farm?

    @Insert
    suspend fun insertFarm(farm: Farm): Long

    @Update
    suspend fun updateFarm(farm: Farm)

    @Delete
    suspend fun deleteFarm(farm: Farm)

    // For Firestore -> Room sync
    @Query("DELETE FROM farms WHERE userId = :userId")
    suspend fun clearFarmsForUser(userId: Long)

    @Insert
    suspend fun insertAll(farms: List<Farm>)
}
