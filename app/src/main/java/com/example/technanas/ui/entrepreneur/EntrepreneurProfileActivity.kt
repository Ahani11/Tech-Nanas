package com.example.technanas.ui.entrepreneur

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.technanas.R
import com.example.technanas.data.model.Farm
import com.example.technanas.databinding.ActivityEntrepreneurProfileBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EntrepreneurProfileActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OWNER_EMAIL = "extra_owner_email"
    }

    private lateinit var binding: ActivityEntrepreneurProfileBinding
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var farmAdapter: PublicFarmAdapter
    private lateinit var shopAdapter: PublicFarmAdapter

    private var ownerEmail: String? = null
    private var phoneNumber: String? = null
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntrepreneurProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Entrepreneur profile"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        ownerEmail = intent.getStringExtra(EXTRA_OWNER_EMAIL)
        if (ownerEmail.isNullOrEmpty()) {
            Toast.makeText(this, "Missing profile info", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Adapters
        farmAdapter = PublicFarmAdapter()
        shopAdapter = PublicFarmAdapter()

        binding.rvFarms.layoutManager = LinearLayoutManager(this)
        binding.rvFarms.adapter = farmAdapter

        binding.rvShops.layoutManager = LinearLayoutManager(this)
        binding.rvShops.adapter = shopAdapter

        loadEntrepreneurProfile(ownerEmail!!)
        loadFarmsAndShops(ownerEmail!!)

        // Call / WhatsApp / Email
        binding.btnCall.setOnClickListener {
            phoneNumber?.let { num ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
                startActivity(intent)
            }
        }

        binding.btnWhatsapp.setOnClickListener {
            phoneNumber?.let { num ->
                val uri = Uri.parse("https://wa.me/$num")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

        binding.btnEmail.setOnClickListener {
            ownerEmail?.let { email ->
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$email")
                }
                startActivity(intent)
            }
        }
    }

    private fun loadEntrepreneurProfile(email: String) {
        lifecycleScope.launch {
            try {
                val doc = firestore.collection("users")
                    .document(email)
                    .get()
                    .await()

                if (!doc.exists()) {
                    Toast.makeText(
                        this@EntrepreneurProfileActivity,
                        "User not found",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val fullName = doc.getString("fullName") ?: email
                val phone = doc.getString("phone")
                val admin = doc.getBoolean("isAdmin") ?: false
                val subRole = doc.getString("subRole")

                val instagramUrl = doc.getString("instagramUrl")
                val instagramActive = doc.getBoolean("instagramActive") ?: false
                val tiktokUrl = doc.getString("tiktokUrl")
                val tiktokActive = doc.getBoolean("tiktokActive") ?: false
                val facebookUrl = doc.getString("facebookUrl")
                val facebookActive = doc.getBoolean("facebookActive") ?: false

                phoneNumber = phone
                isAdmin = admin

                binding.tvName.text = fullName
                binding.tvEmail.text = email

                binding.tvRole.text = when {
                    !subRole.isNullOrBlank() -> subRole
                    admin -> "LPNM (Admin)"
                    else -> "Entrepreneur"
                }

                if (phone.isNullOrEmpty()) {
                    binding.tvPhone.text = "Phone not available"
                    binding.btnCall.isEnabled = false
                    binding.btnWhatsapp.isEnabled = false
                } else {
                    binding.tvPhone.text = phone
                    binding.btnCall.isEnabled = true
                    binding.btnWhatsapp.isEnabled = true
                }

                setupSocialButtons(
                    instagramUrl, instagramActive,
                    tiktokUrl, tiktokActive,
                    facebookUrl, facebookActive
                )

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@EntrepreneurProfileActivity,
                    "Failed to load profile",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupSocialButtons(
        instagramUrl: String?, instagramActive: Boolean,
        tiktokUrl: String?, tiktokActive: Boolean,
        facebookUrl: String?, facebookActive: Boolean
    ) {
        fun setup(button: Button, active: Boolean, url: String?) {
            if (active && !url.isNullOrBlank()) {
                button.visibility = View.VISIBLE
                button.isEnabled = true
                button.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            } else {
                button.visibility = View.GONE
                button.isEnabled = false
                button.setOnClickListener(null)
            }
        }

        setup(binding.btnInstagram, instagramActive, instagramUrl)
        setup(binding.btnTiktok, tiktokActive, tiktokUrl)
        setup(binding.btnFacebook, facebookActive, facebookUrl)

        val anyVisible = listOf(
            binding.btnInstagram.visibility,
            binding.btnTiktok.visibility,
            binding.btnFacebook.visibility
        ).any { it == View.VISIBLE }

        binding.tvSocialTitle.visibility = if (anyVisible) View.VISIBLE else View.GONE
        binding.layoutSocialButtons.visibility = if (anyVisible) View.VISIBLE else View.GONE
    }

    private fun loadFarmsAndShops(email: String) {
        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("farms")
                    .whereEqualTo("ownerEmail", email)
                    .get()
                    .await()

                val farms = mutableListOf<Farm>()
                val shops = mutableListOf<Farm>()

                snapshot.documents.forEach { doc ->
                    try {
                        val name = doc.getString("name") ?: return@forEach
                        val size = doc.getString("size")
                        val state = doc.getString("state")
                        val address = doc.getString("address")
                        val latitude = doc.getDouble("latitude")
                        val longitude = doc.getDouble("longitude")
                        val type = (doc.getString("type") ?: "farm").lowercase()

                        val farm = Farm(
                            id = 0L,
                            userId = 0L,
                            name = name,
                            size = size,
                            state = state,
                            address = address,
                            latitude = latitude,
                            longitude = longitude,
                            remoteId = doc.id
                        )

                        if (type == "shop") {
                            shops.add(farm)
                        } else {
                            farms.add(farm)
                        }
                    } catch (_: Exception) {
                        // ignore malformed docs
                    }
                }

                // Farms section
                if (farms.isEmpty()) {
                    binding.tvNoFarms.visibility = View.VISIBLE
                } else {
                    binding.tvNoFarms.visibility = View.GONE
                    farmAdapter.submitList(farms)
                }

                // Shops section
                if (shops.isEmpty()) {
                    binding.tvNoShops.visibility = View.VISIBLE
                } else {
                    binding.tvNoShops.visibility = View.GONE
                    shopAdapter.submitList(shops)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvNoFarms.visibility = View.VISIBLE
                binding.tvNoShops.visibility = View.VISIBLE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

// Read-only farms / shops adapter for public profile
class PublicFarmAdapter : RecyclerView.Adapter<PublicFarmAdapter.FarmViewHolder>() {

    private val items = mutableListOf<Farm>()

    fun submitList(newList: List<Farm>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): FarmViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_public_farm, parent, false)
        return FarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: FarmViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class FarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: android.widget.TextView = itemView.findViewById(R.id.tvFarmName)
        private val tvLocation: android.widget.TextView = itemView.findViewById(R.id.tvFarmLocation)
        private val tvSize: android.widget.TextView = itemView.findViewById(R.id.tvFarmSize)

        fun bind(farm: Farm) {
            tvName.text = farm.name
            tvSize.text = farm.size ?: ""
            val stateText = farm.state ?: ""
            val addressText = farm.address ?: ""
            tvLocation.text = listOf(stateText, addressText)
                .filter { it.isNotBlank() }
                .joinToString(" • ")
        }
    }
}
