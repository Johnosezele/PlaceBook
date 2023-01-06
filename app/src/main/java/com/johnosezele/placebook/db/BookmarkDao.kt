package com.johnosezele.placebook.db
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE
import com.johnosezele.placebook.model.Bookmark


@Dao
interface BookmarkDao {
    // 2
    @Query("SELECT * FROM Bookmark")
    fun loadAll(): LiveData<List<Bookmark>>
    // 3
    @Query("SELECT * FROM Bookmark WHERE id = :bookmarkId")
    fun loadBookmark(bookmarkId: Long): Bookmark
    @Query("SELECT * FROM Bookmark WHERE id = :bookmarkId")
    fun loadLiveBookmark(bookmarkId: Long): LiveData<Bookmark>
    // 4
    @Insert(onConflict = IGNORE)
    fun insertBookmark(bookmark: Bookmark): Long
    // 5
    @Update(onConflict = REPLACE)
    fun updateBookmark(bookmark: Bookmark)
}