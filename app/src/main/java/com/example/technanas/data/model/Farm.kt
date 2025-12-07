package com.example.technanas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farms")
data class Farm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,          // owner user id (local Room id)
    val name: String,
    val size: String?,         // e.g. "2 acres"
    val state: String?,        // e.g. "Johor"
    val address: String?,      // text description
    val latitude: Double?,     // from Google Maps
    val longitude: Double?,    // from Google Maps

    // Firestore document id so we can update/delete remotely
    val remoteId: String? = null
)
