package com.example.technanas.data.repository

import com.example.technanas.data.dao.UserDao
import com.example.technanas.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class UserResult {
    data class Success(val user: User) : UserResult()
    data class Error(val message: String) : UserResult()
}

class UserRepository(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val COLLECTION_USERS = "users"
    }

    /**
     * Register a new user.
     * - Checks uniqueness in local Room (email).
     * - Writes profile to Firestore (source of truth).
     * - Inserts the same profile into Room (cache).
     *
     * IC number was removed from the model, so no IC checks anymore.
     */
    suspend fun register(user: User): UserResult {
        if (userDao.countByEmail(user.email) > 0) {
            return UserResult.Error("Email already registered.")
        }

        val data = hashMapOf(
            "fullName" to user.fullName,
            "phone" to user.phone,
            "email" to user.email,
            // password is NOT stored in Firestore
            "isAdmin" to user.isAdmin,
            "role" to user.role,
            "subRole" to user.subRole
        )

        return try {
            // 1) Write to Firestore (source of truth)
            firestore.collection(COLLECTION_USERS)
                .document(user.email) // use email as document id
                .set(data)
                .await()

            // 2) Insert into Room
            userDao.insertUser(user)

            // 3) Read back from Room (to get auto-generated id)
            val inserted = userDao.getUserByEmail(user.email)
            if (inserted != null) {
                UserResult.Success(inserted)
            } else {
                UserResult.Error("Failed to save user locally.")
            }
        } catch (e: Exception) {
            UserResult.Error(e.localizedMessage ?: "Failed to save user to server.")
        }
    }

    /**
     * Pull latest profile for this email from Firestore into Room.
     * Safe to call on login / profile screen open.
     */
    suspend fun refreshFromRemote(email: String) {
        val snapshot = firestore.collection(COLLECTION_USERS)
            .document(email)
            .get()
            .await()

        if (!snapshot.exists()) return

        val fullName = snapshot.getString("fullName") ?: return
        val phone = snapshot.getString("phone") ?: ""
        val isAdmin = snapshot.getBoolean("isAdmin") ?: false
        val role = snapshot.getString("role")
            ?: if (isAdmin) User.ROLE_ADMIN else User.ROLE_ENTREPRENEUR
        val subRole = snapshot.getString("subRole")

        val local = userDao.getUserByEmail(email)

        if (local == null) {
            val newUser = User(
                fullName = fullName,
                phone = phone,
                email = email,
                password = "",
                isAdmin = isAdmin,
                role = role,
                subRole = subRole
            )
            userDao.insertUser(newUser)
        } else {
            val updated = local.copy(
                fullName = fullName,
                phone = phone,
                isAdmin = isAdmin,
                role = role,
                subRole = subRole
            )
            userDao.updateUser(updated)
        }
    }

    /**
     * Legacy local login via Room only (kept for compatibility).
     */
    suspend fun login(email: String, password: String): UserResult {
        val user = userDao.getUserByEmail(email) ?: return UserResult.Error("User not found.")
        return if (user.password == password) {
            UserResult.Success(user)
        } else {
            UserResult.Error("Incorrect password.")
        }
    }

    fun getUserByIdFlow(id: Long): Flow<User?> = userDao.getUserByIdFlow(id)

    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)

    /**
     * Update user profile (name/phone/etc.).
     * - Writes to Firestore.
     * - Updates Room.
     */
    suspend fun updateUser(user: User): UserResult {
        val data = hashMapOf(
            "fullName" to user.fullName,
            "phone" to user.phone,
            "email" to user.email,
            "isAdmin" to user.isAdmin,
            "role" to user.role,
            "subRole" to user.subRole
        )

        return try {
            firestore.collection(COLLECTION_USERS)
                .document(user.email)
                .set(data) // upsert
                .await()

            userDao.updateUser(user)
            UserResult.Success(user)
        } catch (e: Exception) {
            UserResult.Error(e.localizedMessage ?: "Failed to update user.")
        }
    }

    // ---------- ENTREPRENEUR LIST (for marketplace) ----------

    /**
     * Stream of all entrepreneurs (non-admin users with role=ENTREPRENEUR) from Room cache.
     * UI layer should call [refreshEntrepreneursFromRemote] before collecting this flow.
     */
    fun getEntrepreneursFlow(): Flow<List<User>> =
        userDao.getUsersByRoleFlow(User.ROLE_ENTREPRENEUR)

    /**
     * Sync entrepreneurs from Firestore into local Room.
     * - Fetches all docs with role = ENTREPRENEUR.
     * - Inserts/updates them in Room.
     * - Does NOT delete old rows (simple sync, enough for uni project).
     */
    suspend fun refreshEntrepreneursFromRemote() {
        val snapshot = firestore.collection(COLLECTION_USERS)
            .whereEqualTo("role", User.ROLE_ENTREPRENEUR)
            .get()
            .await()

        for (doc in snapshot.documents) {
            val email = doc.getString("email") ?: continue
            val fullName = doc.getString("fullName") ?: continue

            val phone = doc.getString("phone") ?: ""
            val isAdmin = doc.getBoolean("isAdmin") ?: false
            val role = doc.getString("role") ?: User.ROLE_ENTREPRENEUR
            val subRole = doc.getString("subRole")

            val local = userDao.getUserByEmail(email)
            if (local == null) {
                val newUser = User(
                    fullName = fullName,
                    phone = phone,
                    email = email,
                    password = "",
                    isAdmin = isAdmin,
                    role = role,
                    subRole = subRole
                )
                userDao.insertUser(newUser)
            } else {
                val updated = local.copy(
                    fullName = fullName,
                    phone = phone,
                    isAdmin = isAdmin,
                    role = role,
                    subRole = subRole
                )
                userDao.updateUser(updated)
            }
        }
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
