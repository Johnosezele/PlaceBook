package com.johnosezele.placebook.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.johnosezele.placebook.R
import com.johnosezele.placebook.databinding.ActivityBookmarkDetailsBinding

class BookmarkDetailsActivity : AppCompatActivity() {
    private lateinit var databinding: ActivityBookmarkDetailsBinding

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
}