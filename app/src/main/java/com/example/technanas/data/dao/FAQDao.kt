package com.example.technanas.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.technanas.data.model.FAQ
import kotlinx.coroutines.flow.Flow

@Dao
interface FAQDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(faqs: List<FAQ>)

    @Query("SELECT * FROM faqs")
    fun getAll(): Flow<List<FAQ>>

    @Query("SELECT * FROM faqs")
    suspend fun getAllOnce(): List<FAQ>

    @Query("SELECT COUNT(*) FROM faqs")
    suspend fun getCount(): Int
}
