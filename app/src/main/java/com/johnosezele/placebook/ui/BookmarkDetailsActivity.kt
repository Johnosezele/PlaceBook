package com.johnosezele.placebook.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.johnosezele.placebook.R
import com.johnosezele.placebook.databinding.ActivityBookmarkDetailsBinding
import com.johnosezele.placebook.viewmodel.BookmarkDetailsViewModel

class BookmarkDetailsActivity : AppCompatActivity() {
    private lateinit var databinding: ActivityBookmarkDetailsBinding

    //initializing the view model
    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null

        override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        databinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_bookmark_details
        )
        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(databinding.toolbar)
    }

    //This method loads the image from bookmarkView and then uses it to set the imageViewPlace
    private fun populateImageView() {
        bookmarkDetailsView?.let { bookmarkView ->
        val placeImage = bookmarkView.getImage(this)
            placeImage?.let {
                databinding.imageViewPlace.setImageBitmap(placeImage)
            }
        }
    }


}