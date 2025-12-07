package com.example.technanas.ui.farm

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.technanas.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var selectedLatLng: LatLng? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Pick farm location"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val btnConfirm = findViewById<Button>(R.id.btnConfirmLocation)
        btnConfirm.setOnClickListener {
            val latLng = selectedLatLng
            if (latLng == null) {
                Toast.makeText(this, "Tap on the map to select a location", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Do reverse geocoding in background to get address
                lifecycleScope.launch {
                    val address = withContext(Dispatchers.IO) {
                        getAddressFromLatLng(latLng)
                    }

                    val data = intent.apply {
                        putExtra("lat", latLng.latitude)
                        putExtra("lng", latLng.longitude)
                        putExtra("address", address)
                    }
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Center on Malaysia by default
        val malaysiaCenter = LatLng(4.2105, 101.9758)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(malaysiaCenter, 5f))

        // Ask permission / enable GPS blue dot & "my location" button
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // When user taps map: set marker & remember selected lat/lng
        map.setOnMapClickListener { latLng ->
            selectedLatLng = latLng
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Selected location")
            )
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    private fun enableMyLocation() {
        val map = googleMap ?: return
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true  // shows small GPS button

        // Try move camera to current device location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val here = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(here, 15f))
            }
        }
    }

    private fun getAddressFromLatLng(latLng: LatLng): String? {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val result = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!result.isNullOrEmpty()) {
                result[0].getAddressLine(0)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
