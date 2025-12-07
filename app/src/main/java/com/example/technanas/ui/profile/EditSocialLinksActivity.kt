package com.example.technanas.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.technanas.TechNanasApp
import com.example.technanas.databinding.ActivityEditSocialLinksBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditSocialLinksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditSocialLinksBinding
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val app by lazy { application as TechNanasApp }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditSocialLinksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Social media links"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val email = app.sessionManager.getUserEmail()
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "No logged-in user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadCurrentLinks(email)

        binding.btnSave.setOnClickListener {
            saveLinks(email)
        }
    }

    private fun loadCurrentLinks(email: String) {
        lifecycleScope.launch {
            try {
                val doc = firestore.collection("users")
                    .document(email)
                    .get()
                    .await()

                if (doc.exists()) {
                    binding.switchInstagram.isChecked =
                        doc.getBoolean("instagramActive") ?: false
                    binding.etInstagram.setText(doc.getString("instagramUrl") ?: "")

                    binding.switchTiktok.isChecked =
                        doc.getBoolean("tiktokActive") ?: false
                    binding.etTiktok.setText(doc.getString("tiktokUrl") ?: "")

                    binding.switchFacebook.isChecked =
                        doc.getBoolean("facebookActive") ?: false
                    binding.etFacebook.setText(doc.getString("facebookUrl") ?: "")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@EditSocialLinksActivity,
                    "Failed to load links",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveLinks(email: String) {
        val instagramUrl = binding.etInstagram.text.toString().trim()
        val tiktokUrl = binding.etTiktok.text.toString().trim()
        val facebookUrl = binding.etFacebook.text.toString().trim()

        val updates = mapOf(
            "instagramUrl" to instagramUrl,
            "instagramActive" to binding.switchInstagram.isChecked,
            "tiktokUrl" to tiktokUrl,
            "tiktokActive" to binding.switchTiktok.isChecked,
            "facebookUrl" to facebookUrl,
            "facebookActive" to binding.switchFacebook.isChecked
        )

        lifecycleScope.launch {
            try {
                firestore.collection("users")
                    .document(email)
                    .update(updates)
                    .await()

                Toast.makeText(
                    this@EditSocialLinksActivity,
                    "Links updated",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@EditSocialLinksActivity,
                    "Failed to save links",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
