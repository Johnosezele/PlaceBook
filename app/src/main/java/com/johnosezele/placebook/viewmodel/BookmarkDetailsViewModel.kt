package com.johnosezele.placebook.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.johnosezele.placebook.model.Bookmark
import com.johnosezele.placebook.repository.BookmarkRepo
import com.johnosezele.placebook.util.ImageUtils

class BookmarkDetailsViewModel(application: Application) :
    AndroidViewModel(application) {
    private val bookmarkRepo = BookmarkRepo(getApplication())


    //BookmarkDetailsView defines the data needed by BookmarkDetailsActivity .
    //getImage() loads the image associated with the bookmark
    data class BookmarkDetailsView(
        var id: Long? = null,
        var name: String = "",
        var phone: String = "",
        var address: String = "",
        var notes: String = ""
    ) {
        fun getImage(context: Context) = id?.let {
            ImageUtils.loadBitmapFromFile(context,
                Bookmark.generateImageFilename(it))
        }
    }
}