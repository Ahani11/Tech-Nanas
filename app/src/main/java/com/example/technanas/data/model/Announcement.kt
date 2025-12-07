package com.example.technanas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val shortDescription: String,
    val fullDescription: String,
    val type: AnnouncementType,
    val dateMillis: Long,
    val externalUrl: String? = null
)
