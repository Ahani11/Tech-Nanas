package com.example.technanas.ui.farm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.technanas.TechNanasApp
import com.example.technanas.data.model.Farm
import com.example.technanas.databinding.ActivityEditFarmBinding
import kotlinx.coroutines.launch

class EditFarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditFarmBinding
    private val app by lazy { application as TechNanasApp }

    private var farmId: Long? = null
    private var selectedLat: Double? = null
    private var selectedLng: Double? = null

    private val MALAYSIA_STATES = listOf(
        "Johor",
        "Kedah",
        "Kelantan",
        "Melaka",
        "Negeri Sembilan",
        "Pahang",
        "Pulau Pinang",
        "Perak",
        "Perlis",
        "Selangor",
        "Terengganu",
        "Sabah",
        "Sarawak",
        "Wilayah Persekutuan Kuala Lumpur",
        "Wilayah Persekutuan Labuan",
        "Wilayah Persekutuan Putrajaya"
    )

    private val SIZE_UNITS = listOf(
        "acre",
        "hectare",
        "square meter",
        "square foot"
    )

    private val mapPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val data = result.data!!
                val lat = data.getDoubleExtra("lat", Double.NaN)
                val lng = data.getDoubleExtra("lng", Double.NaN)
                val address = data.getStringExtra("address")

                if (!lat.isNaN() && !lng.isNaN()) {
                    selectedLat = lat
                    selectedLng = lng
                    binding.tvLocation.text = "Location selected (lat=$lat, lng=$lng)"
                }

                if (!address.isNullOrEmpty()) {
                    // Auto-fill address, user can edit/clear it if they want
                    binding.etFarmAddress.setText(address)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditFarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val userIdInt = app.sessionManager.getUserId()
        if (userIdInt == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val userId = userIdInt.toLong()

        val userEmail = app.sessionManager.getUserEmail()
        if (userEmail.isNullOrEmpty()) {
            Toast.makeText(this, "Missing user email", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // State spinner
        val stateAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            MALAYSIA_STATES
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerState.adapter = stateAdapter

        // Size unit spinner
        val sizeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            SIZE_UNITS
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerSizeUnit.adapter = sizeAdapter

        // Are we editing or adding?
        farmId = intent.getLongExtra("farmId", -1L).takeIf { it != -1L }

        if (farmId != null) {
            title = "Edit farm"
            loadExistingFarm(farmId!!)
        } else {
            title = "Add farm"
        }

        // Open map picker
        binding.btnPickLocation.setOnClickListener {
            val intent = Intent(this, MapPickerActivity::class.java)
            mapPickerLauncher.launch(intent)
        }

        binding.btnSaveFarm.setOnClickListener {
            val name = binding.etFarmName.text.toString().trim()
            val sizeValue = binding.etFarmSizeValue.text.toString().trim()
            val sizeUnit = binding.spinnerSizeUnit.selectedItem as String
            val state = binding.spinnerState.selectedItem as String
            val address = binding.etFarmAddress.text.toString().trim().ifEmpty { null }

            if (name.isEmpty()) {
                binding.etFarmName.error = "Required"
                return@setOnClickListener
            }

            val sizeCombined = if (sizeValue.isEmpty()) {
                null
            } else {
                "$sizeValue $sizeUnit"
            }

            lifecycleScope.launch {
                val repo = app.farmRepository
                if (farmId == null) {
                    val farm = Farm(
                        userId = userId,
                        name = name,
                        size = sizeCombined,
                        state = state,
                        address = address,
                        latitude = selectedLat,
                        longitude = selectedLng
                    )
                    // Firestore first, then Room
                    repo.addFarm(farm, userEmail)
                } else {
                    val existing = repo.getFarmById(farmId!!) ?: return@launch
                    val updated = existing.copy(
                        name = name,
                        size = sizeCombined,
                        state = state,
                        address = address,
                        latitude = selectedLat,
                        longitude = selectedLng
                        // remoteId stays the same because we don't override it here
                    )
                    repo.updateFarm(updated, userEmail)
                }

                Toast.makeText(this@EditFarmActivity, "Saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun loadExistingFarm(id: Long) {
        lifecycleScope.launch {
            val repo = app.farmRepository
            val farm = repo.getFarmById(id) ?: return@launch

            binding.etFarmName.setText(farm.name)

            // parse size "2 acre" -> value + unit
            farm.size?.let { sizeStr ->
                val parts = sizeStr.split(" ", limit = 2)
                if (parts.isNotEmpty()) {
                    binding.etFarmSizeValue.setText(parts[0])
                }
                if (parts.size > 1) {
                    val unitIndex = SIZE_UNITS.indexOf(parts[1])
                    if (unitIndex >= 0) {
                        binding.spinnerSizeUnit.setSelection(unitIndex)
                    }
                }
            }

            val stateIndex = MALAYSIA_STATES.indexOf(farm.state)
            if (stateIndex >= 0) {
                binding.spinnerState.setSelection(stateIndex)
            }

            binding.etFarmAddress.setText(farm.address ?: "")

            if (farm.latitude != null && farm.longitude != null) {
                selectedLat = farm.latitude
                selectedLng = farm.longitude
                binding.tvLocation.text =
                    "Location selected (lat=${farm.latitude}, lng=${farm.longitude})"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
