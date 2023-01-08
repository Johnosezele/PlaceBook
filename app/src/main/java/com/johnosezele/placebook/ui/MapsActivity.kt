package com.johnosezele.placebook.ui

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.johnosezele.placebook.BuildConfig.MAPS_API_KEY
import com.johnosezele.placebook.R
import com.johnosezele.placebook.adapter.BookmarkInfoWindowAdapter
import com.johnosezele.placebook.databinding.ActivityMapsBinding
import com.johnosezele.placebook.viewmodel.MapsViewModel
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }

    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //private var locationRequest: LocationRequest? = null
    private lateinit var binding: ActivityMapsBinding

    private val mapsViewModel by viewModels<MapsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()
        setupPlacesClient()
    }

    private fun setupMapListeners() {
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        map.setOnPoiClickListener {
            displayPoi(it)
        }
        map.setOnInfoWindowClickListener { handleInfoWindowClick(it) }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMapListeners()
        getCurrentLocation()
    }

    private fun setupPlacesClient() {
        Places.initialize(applicationContext, MAPS_API_KEY)
        placesClient = Places.createClient(this)
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
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION
        )
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

    private fun displayPoi(pointOfInterest: PointOfInterest) {
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        val placeId = pointOfInterest.placeId
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                displayPoiGetPhotoStep(place)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG,
                        "Place not found" + exception.message + ", " + "statusCode: " + statusCode
                    )
                }
            }
    }

    private fun displayPoiGetPhotoStep(place: Place) {
        val photoMetadata = place.photoMetadatas?.get(0)
        if (photoMetadata == null) {
            displayPoiDisplayStep(place, null)
            return
        }
        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
            .setMaxWidth(resources.getDimensionPixelSize(R.dimen.default_image_width))
            .setMaxHeight(resources.getDimensionPixelSize(R.dimen.default_image_height))
            .build()

        placesClient.fetchPhoto(photoRequest)
            .addOnSuccessListener { fetchPhotoResponse ->
                val bitmap = fetchPhotoResponse.bitmap
                displayPoiDisplayStep(place, bitmap)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(TAG, "Place not found: " + exception.message + ", " + "statusCode: " + statusCode)
                }
            }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
//        val iconPhoto = if (photo == null) {
//            BitmapDescriptorFactory.defaultMarker()
//        }else{
//            BitmapDescriptorFactory.fromBitmap(photo)
//        }
        val marker = map.addMarker(MarkerOptions()
            .position(place.latLng as LatLng)
            //.icon(iconPhoto)
            .title(place.name)
            .snippet(place.phoneNumber)
        )
        marker?.tag = PlaceInfo(place, photo)
    }

    class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)

    //handle action when user taps the info window for a place
    private fun handleInfoWindowClick(marker: Marker) {
        val placeInfo = (marker.tag as PlaceInfo)
        if (placeInfo.place != null) {
            mapsViewModel.addBookmarkFromPlace(placeInfo.place, placeInfo.image)
        }
        marker.remove()
    }
}









