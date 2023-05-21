package com.johnosezele.placebook.ui

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.johnosezele.placebook.R
import com.johnosezele.placebook.databinding.ActivityBookmarkDetailsBinding
import com.johnosezele.placebook.util.ImageUtils
import com.johnosezele.placebook.viewmodel.BookmarkDetailsViewModel
import java.io.File

class BookmarkDetailsActivity : AppCompatActivity(),
    PhotoOptionDialogFragment.PhotoOptionDialogListener{
    private lateinit var databinding: ActivityBookmarkDetailsBinding

    //hold a reference to the temporary image file when capturing an image
    private var photoFile: File? = null

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

        databinding.imageViewPlace.setOnClickListener{
            replaceImage()
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

    override fun onCaptureClick() {
        //Any previously assigned photoFile is cleared.
        photoFile = null
        //call createUniqueImageFile() to create a uniquely named image File and
        //assign it to photoFile
        //If an exception is thrown, the method returns without doing anything.
        try {
            photoFile = ImageUtils.createUniqueImageFile(this)
        } catch (ex: java.io.IOException){
            return
        }
        //use the ?.let to make sure photoFile is not null before continuing with
        //the rest of the method
        photoFile?.let {photoFile ->
            //called to get a Uri for the temporary photo file
            val photoUri = FileProvider.getUriForFile(this,
            "com.johnosezele.placebook.fileprovider", photoFile)

            //A new Intent is created with the ACTION_IMAGE_CAPTURE action. This Intent is
            //used to display the camera viewfinder and allow the user to snap a new photo.
            val captureIntent =
                Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)

            //The photoUri is added as an extra on the Intent, so the Intent knows where to
            //save the full-size image captured by the user.
            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)

            //Temporary write permissions on the photoUri are given to the Intent
            val intentActivities = packageManager.queryIntentActivities(
                captureIntent, PackageManager.MATCH_DEFAULT_ONLY
            )
            intentActivities.map { it.activityInfo.packageName }
                .forEach { grantUriPermission(it, photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }

            //The Intent is invoked, and the request code REQUEST_CAPTURE_IMAGE is passed in.
            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)
        }
    }

    override fun onPickClick() {
        Toast.makeText(this, "Gallery Pick",
            Toast.LENGTH_SHORT).show()
    }

    //mthd that creates the photo option dialog and displays it to the user
    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }

   //defines the request code to use when processing the camera capture Intent
    companion object{
        private const val REQUEST_CAPTURE_IMAGE = 1
    }

}