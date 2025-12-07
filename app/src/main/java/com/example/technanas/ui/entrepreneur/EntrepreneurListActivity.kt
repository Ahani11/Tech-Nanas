package com.example.technanas.ui.entrepreneur

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.technanas.TechNanasApp
import com.example.technanas.data.model.User
import com.example.technanas.databinding.ActivityEntrepreneurListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EntrepreneurListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntrepreneurListBinding
    private val app by lazy { application as TechNanasApp }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntrepreneurListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Entrepreneurs"

        val adapter = EntrepreneurAdapter { user ->
            EntrepreneurDetailActivity.start(this, user.id)
        }

        binding.rvEntrepreneurs.layoutManager = LinearLayoutManager(this)
        binding.rvEntrepreneurs.adapter = adapter

        // Firestore -> Room sync
        lifecycleScope.launch {
            try {
                app.userRepository.refreshEntrepreneursFromRemote()
            } catch (_: Exception) {
                // If offline, we just show cached data
            }
        }

        // Observe Room cache
        lifecycleScope.launch {
            app.userRepository.getEntrepreneursFlow().collectLatest { entrepreneurs: List<User> ->
                adapter.submitList(entrepreneurs)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
