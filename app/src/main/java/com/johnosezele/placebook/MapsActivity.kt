package com.johnosezele.placebook

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.johnosezele.placebook.databinding.ActivityMapsBinding
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //private var locationRequest: LocationRequest? = null
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getCurrentLocation()
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location Permission Denied")
            }
        }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
    }

    //method to get user current location
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if(ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermissions()
        } else {
//            if (locationRequest == null) {
//                locationRequest?.let{ locationRequest ->
//                     LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).apply {
//                        setMinUpdateIntervalMillis(1000)
//                        setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
//                        setWaitForAccurateLocation(true)
//                    }.build()
//                    val locationCallback = object : LocationCallback() {
//                        override fun onLocationResult(locationResult: LocationResult) {
//                            getCurrentLocation()
//                        }
//                    }
//                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
//                }
//            }
            map.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null){
                    val latLng = LatLng(location.latitude, location.longitude)
//                    map.clear()
//                    map.addMarker(MarkerOptions().position(latLng).title("You are here!"))
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    map.moveCamera(update)
                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
    }
}