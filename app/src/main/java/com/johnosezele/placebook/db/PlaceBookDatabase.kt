package com.johnosezele.placebook.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.johnosezele.placebook.model.Bookmark

// 1
@Database(entities = arrayOf(Bookmark::class), version = 1)
abstract class PlaceBookDatabase : RoomDatabase() {
    // 2
    abstract fun bookmarkDao(): BookmarkDao
    // 3
    companion object {
        // 4
        private var instance: PlaceBookDatabase? = null
        // 5
        fun getInstance(context: Context): PlaceBookDatabase {
            if (instance == null) {
// 6
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlaceBookDatabase::class.java,
                    "PlaceBook").build()
            }
// 7
            return instance as PlaceBookDatabase
        }
    }
}