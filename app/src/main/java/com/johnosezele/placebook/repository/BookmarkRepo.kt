package com.johnosezele.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.johnosezele.placebook.db.BookmarkDao
import com.johnosezele.placebook.db.PlaceBookDatabase
import com.johnosezele.placebook.model.Bookmark

class BookmarkRepo(context: Context) {
    private val db = PlaceBookDatabase.getInstance(context)
    private val bookmarkDao: BookmarkDao = db.bookmarkDao()
    fun addBookmark(bookmark: Bookmark): Long? {
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId
        return newId
    }
    fun createBookmark(): Bookmark {
        return Bookmark()
    }
    val allBookmarks: LiveData<List<Bookmark>>
        get() {
            return bookmarkDao.loadAll()
        }

    //This method returns a live bookmark from the bookmark DAO
    fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark> =
        bookmarkDao.loadLiveBookmark(bookmarkId)

    //updateBookmark() takes in a bookmark and saves it using the bookmark DAO
    fun updateBookmark(bookmark: Bookmark){
        bookmarkDao.updateBookmark(bookmark)
    }

    //getBookmark() takes in the bookmark ID of that updated bookmark and uses the bookmark DAO to load the corresponding bookmark
    fun getBookmark(bookmarkId: Long): Bookmark{
       return bookmarkDao.loadBookmark(bookmarkId)
    }
}

