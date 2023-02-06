package com.johnosezele.placebook.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
            this, R.layout.activity_bookmark_details
        )
        setupToolbar()
            getIntentData()
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

    //When the user taps on the Info window for a bookmark on the maps Activity, it
    //passes the bookmark ID to the details Activity, this method is used to read this Intent data
    //and use it to populate the UI.
    private fun getIntentData(){
        val bookmarkId = intent.getLongExtra(
            MapsActivity.Companion.EXTRA_BOOKMARK_ID, 0)
        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(this,
            {
                it?.let {
                    bookmarkDetailsView = it
                    databinding.bookmarkDetailsView = it
                    populateImageView()
                }
            })
    }

    //override onCreateOptionsMenu and provide items for the Toolbar by loading in
    //the menu_bookmark_details menu
    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    //saveChanges() method takes changes from the text fields and updates the bookmark
    private fun saveChanges(){
        val name = databinding.editTextName.text.toString()
        if(name.isEmpty()){
            return
        }
        bookmarkDetailsView?.let { bookmarkView ->
            bookmarkView.name = databinding.editTextName.text.toString()
            bookmarkView.notes = databinding.editTextNotes.text.toString()
            bookmarkView.address = databinding.editTextAddress.text.toString()
            bookmarkView.phone = databinding.editTextPhone.text.toString()
            bookmarkDetailsViewModel.updateBookmark(bookmarkView)
        }
        finish()
    }

    //onOptionsItemSelected() is used to respond to the user tapping the checkmark menu item
    //check the item.itemId to see if it matches action_save , and if so, saveChanges() is called
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_save -> {
                saveChanges()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }