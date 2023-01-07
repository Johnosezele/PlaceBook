package com.johnosezele.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.johnosezele.placebook.db.BookmarkDao
import com.johnosezele.placebook.db.PlaceBookDatabase
import com.johnosezele.placebook.model.Bookmark

class BookmarkRepo(context: Context) {
    // 2
    private val db = PlaceBookDatabase.getInstance(context)
    private val bookmarkDao: BookmarkDao = db.bookmarkDao()
    // 3
    fun addBookmark(bookmark: Bookmark): Long? {
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId
        return newId
    }
    // 4
    fun createBookmark(): Bookmark {
        return Bookmark()
    }
    // 5
    val allBookmarks: LiveData<List<Bookmark>>
        get() {
            return bookmarkDao.loadAll()
        }
}