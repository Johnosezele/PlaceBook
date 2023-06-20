package com.johnosezele.placebook.model

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.johnosezele.placebook.util.ImageUtils

@Entity
data class Bookmark(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    var placeId: String? = null,
    var name: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var phone: String = "",
    var address: String = "",
    var notes: String = "",
    var category: String = ""
) {
    //setImage() provides the public interface for saving an image for a Bookmark
    fun setImage(image: Bitmap, context: Context){
        //If the bookmark has an id , then the image gets saved to a file.
        id?.let {
            ImageUtils.saveBitmapToFile(context, image, generateImageFilename(it))
        }
    }
    //generateImageFilename() is placed in a companion object so it's available at
    //the class level. This allows another object to load an image without having to
    //load the bookmark from the database.
    companion object {
        fun generateImageFilename(id: Long?): String{
            //generateImageFilename() returns a filename based on a Bookmark ID
            return "bookmark$id.png"
        }
    }
}


