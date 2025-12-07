package com.example.technanas.ui.farm

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.technanas.TechNanasApp
import com.example.technanas.databinding.ActivityMyFarmsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyFarmsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyFarmsBinding
    private val app by lazy { application as TechNanasApp }

    private lateinit var adapter: FarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyFarmsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Use the toolbar from the layout, just like MyAnnouncementsActivity
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My farms"

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val userId = app.sessionManager.getUserId() ?: return
        val userEmail = app.sessionManager.getUserEmail()
        if (userEmail.isNullOrEmpty()) {
            // no email in session – nothing we can sync
            finish()
            return
        }

        adapter = FarmAdapter { farm ->
            // on item click -> edit
            val intent = Intent(this, EditFarmActivity::class.java)
            intent.putExtra("farmId", farm.id)
            startActivity(intent)
        }

        binding.rvFarms.layoutManager = LinearLayoutManager(this)
        binding.rvFarms.adapter = adapter

        binding.fabAddFarm.setOnClickListener {
            val intent = Intent(this, EditFarmActivity::class.java)
            // no farmId -> add new
            startActivity(intent)
        }

        // 1) Firestore -> Room sync for this user
        lifecycleScope.launch {
            try {
                app.farmRepository.refreshFromRemoteForUser(userId, userEmail)
            } catch (e: Exception) {
                // ignore: if offline, we just show existing local cache
            }
        }

        // 2) Observe local Room cache
        lifecycleScope.launch {
            app.farmRepository.getFarmsForUser(userId).collectLatest { farms ->
                adapter.submitList(farms)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
