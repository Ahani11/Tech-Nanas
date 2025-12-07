package com.example.technanas.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.technanas.TechNanasApp
import com.example.technanas.data.repository.UserResult
import com.example.technanas.databinding.ActivityEditProfileBinding
import com.example.technanas.util.ValidationUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val app by lazy { application as TechNanasApp }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val userId = app.sessionManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val user = app.userRepository.getUserByIdFlow(userId).first()
            if (user == null) {
                Toast.makeText(this@EditProfileActivity, "User not found", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // Fill current values
                binding.etFullName.setText(user.fullName)
                binding.etPhone.setText(user.phone)
                binding.tvEmail.text = user.email

                binding.btnSave.setOnClickListener {
                    val name = binding.etFullName.text.toString().trim()
                    val phone = binding.etPhone.text.toString().trim()

                    if (name.isEmpty()) {
                        binding.etFullName.error = "Required"
                        return@setOnClickListener
                    }
                    if (!ValidationUtils.isValidPhone(phone)) {
                        binding.etPhone.error = "Invalid phone"
                        return@setOnClickListener
                    }

                    val updated = user.copy(
                        fullName = name,
                        phone = phone
                    )

                    lifecycleScope.launch {
                        when (app.userRepository.updateUser(updated)) {
                            is UserResult.Success -> {
                                app.sessionManager.updateUserName(name)
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "Profile updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }

                            is UserResult.Error -> {
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "Update failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
