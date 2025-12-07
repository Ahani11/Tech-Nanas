package com.example.technanas.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.technanas.TechNanasApp
import com.example.technanas.data.model.User
import com.example.technanas.data.repository.UserResult
import com.example.technanas.databinding.ActivityRegisterBinding
import com.example.technanas.util.ValidationUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val app by lazy { application as TechNanasApp }
    private val auth by lazy { Firebase.auth }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Register"

        setupRoleUi()

        binding.btnRegister.setOnClickListener {
            attemptRegister()
        }
    }

    private fun setupRoleUi() {
        // Show sub-role options only when "Entrepreneur" is selected
        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.rbEntrepreneur.id) {
                binding.layoutSubRole.visibility = View.VISIBLE
            } else {
                binding.layoutSubRole.visibility = View.GONE
            }
        }
    }

    private fun attemptRegister() {
        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Basic validation
        if (fullName.isEmpty()) {
            binding.etFullName.error = "Required"
            binding.etFullName.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            binding.etPhone.error = "Required"
            binding.etPhone.requestFocus()
            return
        }

        if (!ValidationUtils.isValidEmail(email)) {
            binding.etEmail.error = "Invalid email"
            binding.etEmail.requestFocus()
            return
        }

        if (!ValidationUtils.isValidPassword(password)) {
            binding.etPassword.error = "Password too short"
            binding.etPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            binding.etConfirmPassword.requestFocus()
            return
        }

        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, "You must agree to the terms", Toast.LENGTH_SHORT).show()
            return
        }

        // Determine role
        val role: String
        val subRole: String?

        when (binding.rgRole.checkedRadioButtonId) {
            binding.rbEntrepreneur.id -> {
                role = User.ROLE_ENTREPRENEUR

                subRole = when (binding.rgSubRole.checkedRadioButtonId) {
                    binding.rbFarmer.id -> "FARMER"
                    binding.rbWholesaler.id -> "WHOLESALER"
                    binding.rbRetailer.id -> "RETAILER"
                    else -> {
                        Toast.makeText(
                            this,
                            "Please choose whether you are a farmer, wholesaler or retailer.",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }
                }
            }

            binding.rbBuyer.id -> {
                role = User.ROLE_BUYER
                subRole = null
            }

            else -> {
                Toast.makeText(
                    this,
                    "Please select whether you are an entrepreneur or buyer.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        // Now create user with Firebase Auth
        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (!authTask.isSuccessful) {
                    setLoading(false)
                    val msg = authTask.exception?.localizedMessage ?: "Registration failed."
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                // Send verification email
                val firebaseUser = auth.currentUser
                firebaseUser?.sendEmailVerification()

                // Save profile to Firestore + Room via UserRepository
                lifecycleScope.launch {
                    val user = User(
                        fullName = fullName,
                        phone = phone,
                        email = email,
                        password = password,
                        isAdmin = false,
                        role = role,
                        subRole = subRole
                    )

                    val result = app.userRepository.register(user)

                    withContext(Dispatchers.Main) {
                        when (result) {
                            is UserResult.Success -> {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Registration successful. Please check your email to verify your account.",
                                    Toast.LENGTH_LONG
                                ).show()
                                setLoading(false)
                                finish() // go back to LoginActivity
                            }

                            is UserResult.Error -> {
                                // If profile save fails, delete auth user so it doesn't get stuck
                                auth.currentUser?.delete()
                                Toast.makeText(
                                    this@RegisterActivity,
                                    result.message,
                                    Toast.LENGTH_LONG
                                ).show()
                                setLoading(false)
                            }
                        }
                    }
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
