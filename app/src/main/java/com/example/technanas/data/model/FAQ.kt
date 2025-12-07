package com.example.technanas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faqs")
data class FAQ(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val question: String,
    val answer: String,
    val category: FAQCategory,
    val keywords: String
)


