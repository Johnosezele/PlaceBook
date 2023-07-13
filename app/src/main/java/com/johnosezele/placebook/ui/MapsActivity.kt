package com.johnosezele.placebook.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.johnosezele.placebook.BuildConfig.MAPS_API_KEY
import com.johnosezele.placebook.R
import com.johnosezele.placebook.adapter.BookmarkInfoWindowAdapter
import com.johnosezele.placebook.adapter.BookmarkListAdapter
import com.johnosezele.placebook.databinding.ActivityMapsBinding
import com.johnosezele.placebook.viewmodel.MapsViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val EXTRA_BOOKMARK_ID = "com.johnosezele.placebook.EXTRA_BOOKMARK_ID"
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
        private const val AUTOCOMPLETE_REQUEST_CODE = 2
    }

    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //private var locationRequest: LocationRequest? = null
    private lateinit var binding: ActivityMapsBinding

    private val mapsViewModel by viewModels<MapsViewModel>()

    //bookmarksList Adapter for navdrawer
    private lateinit var bookmarkListAdapter: BookmarkListAdapter

    private var markers = HashMap<Long, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupToolbar()
        setupLocationClient()
        setupPlacesClient()
        setupNavigationDrawer()
    }

    //Enable support toolbar in MapsActivity
    private fun setupToolbar(){
        setSupportActionBar(binding.mainMapView.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout,
            binding.mainMapView.toolbar,
            R.string.open_drawer, R.string.close_drawer
        )
        toggle.syncState()
    }

    private fun setupMapListeners() {
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        map.setOnPoiClickListener {
            displayPoi(it)
        }
        map.setOnInfoWindowClickListener { handleInfoWindowClick(it) }
        binding.mainMapView.fab.setOnClickListener {
            searchAtCurrentLocation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMapListeners()
        createBookmarkObserver()
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
            Place.Field.LAT_LNG,
            Place.Field.TYPES
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
        marker?.showInfoWindow()
    }

    class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)

    //This method handles the action when a user taps a place Info window.
    //it saves the bookmark if it hasn't been saved before, or it starts the
    // bookmark details Activity if the bookmark has already been saved.
    private fun handleInfoWindowClick(marker: Marker) {
        when (marker.tag){
            is PlaceInfo -> {
                val placeInfo = (marker.tag as PlaceInfo)
                if (placeInfo.place != null && placeInfo.image != null) {
                    GlobalScope.launch {
                        mapsViewModel.addBookmarkFromPlace(placeInfo.place, placeInfo.image)
                    }
                }
                marker.remove()
            }
            is MapsViewModel.BookmarkView -> {
                val bookmarkView = (marker.tag as MapsViewModel.BookmarkView)
                marker.hideInfoWindow()
                bookmarkView.id?.let { startBookmarkDetails(it) }
            }
        }
    }

    //handling the addBookmark() method in MapsViewModel
    private fun newBookmark(latLng: LatLng) {
        GlobalScope.launch {
            val bookmarkId = mapsViewModel.addBookmark(latLng)
            bookmarkId?.let {
                startBookmarkDetails(it)
            }
        }
    }

    //method to add a bookmark marker to the map
    //This is a helper method that adds a single blue marker to the map based on a
    //BookmarkMarkerView.
    private fun addPlaceMarker(
        bookmark: MapsViewModel.BookmarkView): Marker? {
        val marker = map.addMarker(MarkerOptions()
            .position(bookmark.location)
            .title(bookmark.name)
            .snippet(bookmark.phone)
            .icon(bookmark.categoryResourceId?.let {
                BitmapDescriptorFactory.fromResource(it)
            })
            .alpha(0.8f))

        if (marker != null) {
            marker.tag = bookmark
        }

        bookmark.id?.let {
            if (marker != null) {
                markers.put(it, marker)
            }
        }

        return marker
    }

    //method to display all of the bookmark markers
//    This method walks through a list of BookmarkMarkerView objects and calls
//    addPlaceMarker() for each one.
    private fun displayAllBookmarks(
        bookmarks: List<MapsViewModel.BookmarkView>
    ){
        bookmarks.forEach { addPlaceMarker(it) }
    }

    //method that observes the changes to the bookmark
    //views in the maps View model.
    //This method observes changes to the BookmarkView objects from the
    //MapsViewModel and updates the View when they change
    private fun createBookmarkObserver(){
        mapsViewModel.getBookmarkViews()?.observe(
            this, {
                map.clear()
                markers.clear()
                it?.let { displayAllBookmarks(it) }
                bookmarkListAdapter.setBookmarkData(it)
            })
    }

    //startBookmarkDetails() is used to start the BookmarkDetailsActivity using an explicit Intent .
    private fun startBookmarkDetails(bookmarkId: Long){
        val intent = Intent(this, BookmarkDetailsActivity::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID, bookmarkId)
        startActivity(intent)
    }


    //This method sets up the adapter for the bookmark recycler view. It gets the
    //RecyclerView from the Layout, sets a default LinearLayoutManager for the
    //RecyclerView , then creates a new BookmarkListAdapter and assigns it to the
    //RecyclerView
    private fun setupNavigationDrawer() {
        val layoutManager = LinearLayoutManager(this)
        binding.drawerViewMaps.bookmarkRecyclerView.layoutManager = layoutManager
        bookmarkListAdapter = BookmarkListAdapter(null, this)
        binding.drawerViewMaps.bookmarkRecyclerView.adapter = bookmarkListAdapter
    }

    //helper method to zoom the map to a specific location
    private fun updateMapToLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
    }

    //method that moves the map to a bookmark location
    fun moveToBookmark(bookmark: MapsViewModel.BookmarkView){
        //before zooming the bookmark, the nav drawer is closed
        binding.drawerLayout.closeDrawer(binding.drawerViewMaps.drawerView)
        //markers HashMap is used to look up the Marker
        val marker = markers[bookmark.id]
        //if marker is found, its info window is shown
        marker?.showInfoWindow()
        //A Location object is created from the bookmark, and
        //updateMapToLocation() is called to zoom the map to the bookmark
        val location = Location("")
        location.latitude = bookmark.location.latitude
        location.longitude = bookmark.location.longitude
        updateMapToLocation(location)
    }

    private fun searchAtCurrentLocation() {

        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.TYPES)

        val bounds =
            RectangularBounds.newInstance(map.projection.visibleRegion.latLngBounds)
        try {
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, placeFields)
                .setLocationBias(bounds)
                .build(this)
    startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        } catch (e: GooglePlayServicesRepairableException) {
            Toast.makeText(this, "Problems Searching",
                Toast.LENGTH_LONG).show()
        } catch (e: GooglePlayServicesNotAvailableException) {
            Toast.makeText(this, "Problems Searching. Google Play Not available", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AUTOCOMPLETE_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    val location = Location("")
                    location.latitude = place.latLng?.latitude ?: 0.0
                    location.longitude = place.latLng?.longitude ?: 0.0
                    updateMapToLocation(location)
                    displayPoiGetPhotoStep(place)
                }
        }
    }
}









