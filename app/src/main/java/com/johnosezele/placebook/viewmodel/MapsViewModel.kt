package com.johnosezele.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.johnosezele.placebook.model.Bookmark
import com.johnosezele.placebook.repository.BookmarkRepo
import com.johnosezele.placebook.util.ImageUtils

class MapsViewModel(application: Application) :
    AndroidViewModel(application) {
    private val TAG = "MapsViewModel"

    //LiveData object that wraps a list of BookmarkMarkerView
    //objects
    private var bookmarks: LiveData<List<BookmarkView>>? = null

    private val bookmarkRepo: BookmarkRepo = BookmarkRepo(
        getApplication()
    )

    fun addBookmarkFromPlace(place: Place, image: Bitmap?) {
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.name = place.name.toString()
        bookmark.longitude = place.latLng?.longitude ?: 0.0
        bookmark.latitude = place.latLng?.latitude ?: 0.0
        bookmark.phone = place.phoneNumber?.toString().toString()
        bookmark.address = place.address?.toString().toString()

        val newId = bookmarkRepo.addBookmark(bookmark)
        image?.let { bookmark.setImage(it, getApplication()) }
        Log.i(TAG, "New bookmark $newId added to the database.")
    }

    //class to store bookmark marker views
    //This will hold the information needed by the View to plot a marker for a single bookmark.
    data class BookmarkView(
        var id: Long? = null,
        var location: LatLng = LatLng(0.0, 0.0),
        var name: String = "",
        var phone: String = ""
    ) {
        fun getImage(context: Context) = id?.let {
            ImageUtils.loadBitmapFromFile(context,
                Bookmark.generateImageFilename(it))
        }
    }

    //method to populate book marker views from the bookmarks stored in db
    //this helper method that converts a Bookmark object from the repo into a
    //BookmarkMarkerView object
    private fun bookmarkToBookmarkView(bookmark: Bookmark) =
         BookmarkView(
             bookmark.id,
             LatLng(bookmark.latitude, bookmark.longitude),
             bookmark.name,
             bookmark.phone
         )

    //This method maps the LiveData<List<Bookmark>> objects provided by
    //BookmarkRepo into LiveData<List<BookmarkMarkerView>> objects that can
    //be used by MapsActivity
    private fun mapBookmarksToBookmarkView(){
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks)
        { repoBookmarks ->
            repoBookmarks.map { bookmark ->
                bookmarkToBookmarkView(bookmark)
            }
        }
    }

    //method to initialize and return the bookmark
    //marker views to the MapsActivity.
    //This method returns the LiveData object that will be observed by MapsActivity
    fun getBookmarkViews() : LiveData<List<BookmarkView>>? {
        if (bookmarks == null) {
            mapBookmarksToBookmarkView()
        }
        return bookmarks
    }

}