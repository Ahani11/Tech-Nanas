package com.example.technanas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String,
    val email: String,
    val password: String,

    // Legacy admin flag (you already use this)
    val isAdmin: Boolean = false,

    // High-level role in the app
    // "ADMIN", "ENTREPRENEUR", or "BUYER"
    val role: String = "ENTREPRENEUR",

    // Optional sub-role for entrepreneurs
    // e.g. "FARMER", "WHOLESALER", "RETAILER"
    val subRole: String? = null
) {
    companion object {
        const val ROLE_ADMIN = "ADMIN"
        const val ROLE_ENTREPRENEUR = "ENTREPRENEUR"
        const val ROLE_BUYER = "BUYER"
    }
}
