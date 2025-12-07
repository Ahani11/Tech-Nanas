package com.example.technanas.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.technanas.TechNanasApp
import com.example.technanas.databinding.ActivityLoginBinding
import com.example.technanas.util.ValidationUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val app by lazy { application as TechNanasApp }
    private val auth by lazy { Firebase.auth }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { attemptLogin() }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (!ValidationUtils.isValidEmail(email)) {
            binding.etEmail.error = "Invalid email"
            return
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.etPassword.error = "Password too short"
            return
        }

        setLoading(true)

        // 1) Login with Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    setLoading(false)
                    val msg = task.exception?.localizedMessage ?: "Login failed"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                val firebaseUser = auth.currentUser
                if (firebaseUser == null) {
                    setLoading(false)
                    Toast.makeText(
                        this,
                        "Login failed: no Firebase user.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnCompleteListener
                }

                // 2) Enforce email verification
                if (!firebaseUser.isEmailVerified) {
                    auth.signOut()
                    setLoading(false)
                    Toast.makeText(
                        this,
                        "Your email is not verified. Please check your inbox and click the verification link.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnCompleteListener
                }

                // 3) Sync user profile from Firestore -> Room, then read from Room
                lifecycleScope.launch {
                    try {
                        app.userRepository.refreshFromRemote(email)
                    } catch (e: Exception) {
                        // If offline or Firestore fails, we simply fall back to whatever is in Room.
                    }

                    val user = app.userRepository.getUserByEmail(email)

                    if (user == null) {
                        withContext(Dispatchers.Main) {
                            setLoading(false)
                            Toast.makeText(
                                this@LoginActivity,
                                "User profile not found. Please register in this app first.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }

                    // Save session (userId, name, email, isAdmin, role, subRole)
                    app.sessionManager.saveLoginSession(
                        user.id,
                        user.fullName,
                        user.email,
                        user.isAdmin,
                        user.role,
                        user.subRole
                    )

                    withContext(Dispatchers.Main) {
                        setLoading(false)
                        Toast.makeText(
                            this@LoginActivity,
                            "Welcome ${user.fullName}",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }
}
