package com.example.technanas.ui.announcements

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.technanas.TechNanasApp
import com.example.technanas.data.model.Announcement
import com.example.technanas.data.model.AnnouncementType
import com.example.technanas.databinding.ActivityAddEditAnnouncementBinding
import kotlinx.coroutines.launch

class AddEditAnnouncementActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ANNOUNCEMENT_ID = "extra_announcement_id"
    }

    private lateinit var binding: ActivityAddEditAnnouncementBinding
    private val app by lazy { application as TechNanasApp }

    private var announcementId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        announcementId = intent.getLongExtra(EXTRA_ANNOUNCEMENT_ID, -1L)
            .takeIf { it != -1L }

        if (announcementId == null) {
            supportActionBar?.title = "Add announcement"
            binding.btnDelete.visibility = View.GONE
        } else {
            supportActionBar?.title = "Edit announcement"
            binding.btnDelete.visibility = View.VISIBLE
            loadAnnouncement(announcementId!!)
        }

        binding.btnSave.setOnClickListener {
            saveAnnouncement()
        }

        binding.btnDelete.setOnClickListener {
            deleteAnnouncement()
        }
    }

    private fun loadAnnouncement(id: Long) {
        lifecycleScope.launch {
            val repo = app.announcementRepository
            val ann = repo.getAnnouncementById(id)
            if (ann != null) {
                binding.etTitle.setText(ann.title)
                binding.etShortDescription.setText(ann.shortDescription)
                binding.etFullDescription.setText(ann.fullDescription)
            }
        }
    }

    private fun saveAnnouncement() {
        val titleText = binding.etTitle.text.toString().trim()
        val shortDesc = binding.etShortDescription.text.toString().trim()
        val fullDesc = binding.etFullDescription.text.toString().trim()

        if (titleText.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
            return
        }

        // who is the owner?
        val ownerEmail = app.sessionManager.getUserEmail()
        if (ownerEmail.isNullOrEmpty()) {
            Toast.makeText(this, "No logged-in user", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val repo = app.announcementRepository

            if (announcementId == null) {
                // New announcement
                val newAnn = Announcement(
                    title = titleText,
                    shortDescription = shortDesc,
                    fullDescription = fullDesc,
                    type = AnnouncementType.EVENT, // default for now
                    dateMillis = System.currentTimeMillis(),
                    externalUrl = null
                )
                repo.insertAnnouncement(newAnn, ownerEmail)
            } else {
                // Update existing (local-only)
                val existing = repo.getAnnouncementById(announcementId!!)
                if (existing != null) {
                    val updated = existing.copy(
                        title = titleText,
                        shortDescription = shortDesc,
                        fullDescription = fullDesc
                    )
                    repo.updateAnnouncement(updated)
                }
            }

            Toast.makeText(this@AddEditAnnouncementActivity, "Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun deleteAnnouncement() {
        announcementId?.let { id ->
            lifecycleScope.launch {
                val repo = app.announcementRepository
                val existing = repo.getAnnouncementById(id)
                if (existing != null) {
                    repo.deleteAnnouncement(existing)
                    Toast.makeText(
                        this@AddEditAnnouncementActivity,
                        "Deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
