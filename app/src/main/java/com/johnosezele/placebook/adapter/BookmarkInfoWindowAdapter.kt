package com.johnosezele.placebook.adapter
import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.johnosezele.placebook.databinding.ContentBookmarkInfoBinding
import com.johnosezele.placebook.ui.MapsActivity
import com.johnosezele.placebook.viewmodel.MapsViewModel

class BookmarkInfoWindowAdapter(context: Activity) : GoogleMap.InfoWindowAdapter {
    private val binding = ContentBookmarkInfoBinding.inflate(context.layoutInflater)

    override fun getInfoContents(marker: Marker): View {
        binding.title.text = marker.title ?: ""
        binding.phone.text = marker.snippet ?: ""
        val imageView = binding.photo
        //imageView.setImageBitmap((marker.tag as MapsActivity.PlaceInfo).image)
        //this code is replaced bc: The problem is that this code assumes that marker.tag contains an object of type
        //MapsActivity.PlaceInfo . However, that's not always the case because a marker can
        //now represent two types of places: one is a temporary place that isn't bookmarked
        //yet, and the other is a place that has an existing bookmark. Updated code below
        when (marker.tag) {
            is MapsActivity.PlaceInfo -> {
                imageView.setImageBitmap((marker.tag as MapsActivity.PlaceInfo).image)
            }
            is MapsViewModel.BookmarkMarkerView -> {
                val bookMarkView = marker.tag as MapsViewModel.BookmarkMarkerView
                //set ImageView bitmap here
            }
        }
        return binding.root
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

}