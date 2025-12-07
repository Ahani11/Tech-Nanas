package com.example.technanas.ui.entrepreneur

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.technanas.TechNanasApp
import com.example.technanas.data.model.User
import com.example.technanas.databinding.ActivityEntrepreneurDetailBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EntrepreneurDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntrepreneurDetailBinding
    private val app by lazy { application as TechNanasApp }

    private var currentPhone: String? = null
    private var currentEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntrepreneurDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        if (userId == -1L) {
            finish()
            return
        }

        val farmAdapter = EntrepreneurFarmAdapter()
        binding.rvFarms.layoutManager = LinearLayoutManager(this)
        binding.rvFarms.adapter = farmAdapter

        // Observe entrepreneur profile
        lifecycleScope.launch {
            app.userRepository.getUserByIdFlow(userId).collectLatest { user ->
                if (user != null) {
                    bindUser(user)
                }
            }
        }

        // Observe this entrepreneur's farms / stores
        lifecycleScope.launch {
            app.farmRepository.getFarmsForUser(userId).collectLatest { farms ->
                farmAdapter.submitList(farms)
            }
        }

        binding.btnCall.setOnClickListener {
            currentPhone?.let { phone ->
                if (phone.isNotBlank()) {
                    val uri = Uri.parse("tel:$phone")
                    val intent = Intent(Intent.ACTION_DIAL, uri)
                    startActivity(intent)
                }
            }
        }

        binding.btnWhatsApp.setOnClickListener {
            currentPhone?.let { phone ->
                if (phone.isNotBlank()) {
                    // Basic WhatsApp link using phone number
                    val uri = Uri.parse("https://wa.me/$phone")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
        }
    }

    private fun bindUser(user: User) {
        supportActionBar?.title = user.fullName

        binding.tvName.text = user.fullName

        val businessType = when (user.subRole) {
            "FARMER" -> "Farmer"
            "WHOLESALER" -> "Wholesaler"
            "RETAILER" -> "Retailer"
            else -> "Entrepreneur"
        }
        binding.tvBusinessType.text = businessType

        binding.tvEmail.text = user.email
        binding.tvPhone.text = user.phone

        currentPhone = user.phone
        currentEmail = user.email
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"

        fun start(context: Context, userId: Long) {
            val intent = Intent(context, EntrepreneurDetailActivity::class.java)
            intent.putExtra(EXTRA_USER_ID, userId)
            context.startActivity(intent)
        }
    }
}
