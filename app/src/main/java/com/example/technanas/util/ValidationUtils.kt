package com.example.technanas.util

import android.util.Patterns

object ValidationUtils {

    // ✅ Name: not blank and at least 3 characters
    fun isValidName(name: String): Boolean =
        name.trim().length >= 3

    // ✅ IC: not blank and reasonable length (adjust if your course requires specific format)
    fun isValidIc(ic: String): Boolean =
        ic.isNotBlank() && ic.length in 6..20

    // ✅ Phone: keep your old rule, maybe limit max length a bit
    fun isValidPhone(phone: String): Boolean =
        phone.isNotBlank() && phone.length in 8..15

    // ✅ Email: same as before
    fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    // ✅ Password: at least 6 chars (same rule used in Login/Register)
    fun isValidPassword(password: String): Boolean =
        password.length >= 6
}
