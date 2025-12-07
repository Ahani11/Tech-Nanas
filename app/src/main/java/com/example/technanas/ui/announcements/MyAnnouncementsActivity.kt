package com.example.technanas.ui.announcements

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.technanas.TechNanasApp
import com.example.technanas.databinding.ActivityMyAnnouncementsBinding
import kotlinx.coroutines.launch

class MyAnnouncementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAnnouncementsBinding
    private val app by lazy { application as TechNanasApp }

    private lateinit var myAdapter: MyAnnouncementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAnnouncementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar with back arrow
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My announcements"

        val email = app.sessionManager.getUserEmail()
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "No logged-in user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // RecyclerView
        myAdapter = MyAnnouncementsAdapter()
        binding.rvMyAnnouncements.apply {
            layoutManager = LinearLayoutManager(this@MyAnnouncementsActivity)
            adapter = myAdapter
        }

        // FAB -> open AddEditAnnouncementActivity for NEW announcement
        binding.fabAddAnnouncement.setOnClickListener {
            startActivity(Intent(this, AddEditAnnouncementActivity::class.java))
        }

        // initial load
        loadMyAnnouncements(email)
    }

    override fun onResume() {
        super.onResume()
        // refresh list when coming back from Add/Edit screen
        val email = app.sessionManager.getUserEmail()
        if (!email.isNullOrEmpty()) {
            loadMyAnnouncements(email)
        }
    }

    private fun loadMyAnnouncements(email: String) {
        binding.tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val list = app.announcementRepository.getAnnouncementsForOwner(email)
                if (list.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    myAdapter.submitList(emptyList())
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    myAdapter.submitList(list)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@MyAnnouncementsActivity,
                    "Failed to load announcements",
                    Toast.LENGTH_SHORT
                ).show()
                binding.tvEmpty.visibility = View.VISIBLE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
