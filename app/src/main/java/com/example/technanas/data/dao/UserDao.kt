package com.example.technanas.data.dao

import androidx.room.*
import com.example.technanas.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Long): Flow<User?>

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun countByEmail(email: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE isAdmin = 1")
    suspend fun getAdminCount(): Int

    // Stream of all entrepreneurs (non-admin users with role = ENTREPRENEUR)
    @Query("SELECT * FROM users WHERE role = :role AND isAdmin = 0 ORDER BY fullName ASC")
    fun getUsersByRoleFlow(role: String): Flow<List<User>>
}
