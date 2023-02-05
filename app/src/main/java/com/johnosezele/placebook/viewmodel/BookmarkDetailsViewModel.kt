package com.johnosezele.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.view.animation.Transformation
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.johnosezele.placebook.model.Bookmark
import com.johnosezele.placebook.repository.BookmarkRepo
import com.johnosezele.placebook.util.ImageUtils

class BookmarkDetailsViewModel(application: Application) :
    AndroidViewModel(application) {
    private val bookmarkRepo = BookmarkRepo(getApplication())

    //The bookmarkDetailsView property holds the LiveData<BookmarkDetailsView>
    //object. This allows the View to stay updated anytime the view model changes.
    private var bookmarkDetailsView: LiveData<BookmarkDetailsView>? = null

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

    //This method converts a Bookmark model to a BookmarkDetailsView model.
    private fun bookmarkToBookmarkView(bookmark: Bookmark):
        BookmarkDetailsView{
            return BookmarkDetailsView(
                bookmark.id,
                bookmark.name,
                bookmark.phone,
                bookmark.address,
                bookmark.notes
            )
        }

    //Here, you get the live Bookmark from the BookmarkRepo and then transform it to the
    //live BookmarkDetailsView
    private fun mapBookmarkToBookmarkView(bookmarkId: Long){
        val bookmark = bookmarkRepo.getLiveBookmark(bookmarkId)
        bookmarkDetailsView = Transformations.map(bookmark)
        {
            repoBookmark ->
            bookmarkToBookmarkView(repoBookmark)
        }
    }

    //getBookmark() returns a live bookmark View based on a bookmark ID
    fun getBookmark(bookmarkId: Long) :LiveData<BookmarkDetailsView>? {
            if (bookmarkDetailsView == null) {
                mapBookmarkToBookmarkView(bookmarkId)
            }
            return bookmarkDetailsView
        }

    //When the user makes changes to a bookmark, you need to update the bookmark view
    //model class. For that, you need a method to convert a bookmark view model to the
    //database bookmark model.

    //bookmarkViewToBookmark() takes a BookmarkDetailsView and returns a Bookmark with the
    //updated parameters from the BookmarkDetailsView . You load the original
    //bookmark values from the BookmarkRepo before updating them with the BookmarkDetailsView

    private fun bookmarkViewToBookmark(bookmarkView: BookmarkDetailsView): Bookmark?{
        val bookmark = bookmarkView.id?.let {
            bookmarkRepo.getBookmark(it)
        }
        if (bookmark != null){
            bookmark.id = bookmarkView.id
            bookmark.name = bookmarkView.name
            bookmark.phone = bookmarkView.phone
            bookmark.address = bookmarkView.address
            bookmark.notes = bookmark.notes
        }
        return bookmark
    }

    }

