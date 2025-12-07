package com.example.technanas.ui.announcements

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.technanas.TechNanasApp
import com.example.technanas.data.model.Announcement
import com.example.technanas.databinding.ActivityAnnouncementDetailBinding
import com.example.technanas.util.DateUtils
import kotlinx.coroutines.launch

class AnnouncementDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ANNOUNCEMENT_ID = "announcement_id"
    }

    private lateinit var binding: ActivityAnnouncementDetailBinding
    private val app by lazy { application as TechNanasApp }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnnouncementDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val id = intent.getLongExtra(EXTRA_ANNOUNCEMENT_ID, -1L)
        if (id == -1L) {
            Toast.makeText(this, "Announcement not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val announcement = app.announcementRepository.getAnnouncementById(id)
            if (announcement == null) {
                Toast.makeText(this@AnnouncementDetailActivity, "Announcement not found", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                showAnnouncement(announcement)
            }
        }
    }

    private fun showAnnouncement(ann: Announcement) {
        binding.tvTitle.text = ann.title
        binding.tvType.text = ann.type.name
        binding.tvDate.text = DateUtils.formatDate(ann.dateMillis)
        binding.tvFullDescription.text = ann.fullDescription

        if (!ann.externalUrl.isNullOrBlank()) {
            binding.btnOpenLink.visibility = View.VISIBLE
            binding.btnOpenLink.setOnClickListener {
                val url = if (ann.externalUrl.startsWith("http")) ann.externalUrl else "https://${ann.externalUrl}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        } else {
            binding.btnOpenLink.visibility = View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
