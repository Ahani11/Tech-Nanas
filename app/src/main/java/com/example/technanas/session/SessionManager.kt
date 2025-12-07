package com.example.technanas.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tech_nanas_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_ADMIN = "is_admin"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_SUBROLE = "user_subrole"
    }

    fun saveLoginSession(
        userId: Long,
        fullName: String,
        email: String,
        isAdmin: Boolean,
        role: String,
        subRole: String?
    ) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, fullName)
            .putString(KEY_USER_EMAIL, email)
            .putBoolean(KEY_IS_ADMIN, isAdmin)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_SUBROLE, subRole)
            .apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_IS_ADMIN)
            .remove(KEY_USER_ROLE)
            .remove(KEY_USER_SUBROLE)
            .apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): Long? {
        val id = prefs.getLong(KEY_USER_ID, -1L)
        return if (id == -1L) null else id
    }

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun isAdmin(): Boolean = prefs.getBoolean(KEY_IS_ADMIN, false)

    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, null)

    fun getUserSubRole(): String? = prefs.getString(KEY_USER_SUBROLE, null)

    fun updateUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun updateUserRole(role: String, subRole: String?) {
        prefs.edit()
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_SUBROLE, subRole)
            .apply()
    }
}
