package com.johnosezele.placebook.util

import android.content.Context
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.lang.Exception

object ImageUtils {
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        //saveBytesToFile() is called to write the bytes to a file.
        saveBytesToFile(context, bytes, filename)
    }
    private fun saveBytesToFile(context: Context, bytes: ByteArray, filename: String){
        val outputStream: FileOutputStream
        try {
            //openFileOutput is used to open a FileOutputStream using the given filename .
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
            //The bytes are written to the outputStream and then the stream is closed.
            outputStream.write(bytes)
            outputStream.close()
        } catch (e: Exception) { e.printStackTrace()}
    }
}