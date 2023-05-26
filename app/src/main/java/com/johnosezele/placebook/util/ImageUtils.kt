package com.johnosezele.placebook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.jvm.Throws

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
    //load the image from a file
    fun loadBitmapFromFile(context: Context, filename: String): Bitmap? {
        val filePath = File(context.filesDir, filename).absolutePath
        return BitmapFactory.decodeFile(filePath)
    }

    //helper method to generate a unique image filename
    @Throws(IOException::class)
    fun createUniqueImageFile(context: Context): File{
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val filename = "PlaceBook_" + timestamp + "_"
        val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(filename, ".jpg", filesDir)
    }

    //code to downsample the photo to match the default bookmark photo size

    //This method is used to calculate the optimum inSampleSize that can be used to
    //resize an image to a specified width and height.
    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    //Now that you can calculate the proper sample size for any width and height, a new
    //method can be added to decode a file

    //This method is called by BookmarkDetailsActivity to get the downsampled image
    //with a specific width and height from the captured photo file
    fun decodeFileToSize (
        filePath: String,
        width: Int,
        height: Int
    ): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        options.inSampleSize = calculateInSampleSize(
            options.outWidth, options.outHeight, width, height
        )
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(filePath, options)
    }

    //Many times when a user takes a photo in portrait mode, the image will come in
    //rotated. To fix that create a rotation method that will rotate the bitmap to look
    //correct if rotated incorrectly.
    private fun rotateImage(img: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0,0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    //method that will check the File meta data to check it's orientation
    @Throws(IOException::class)
    fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {
        val input: InputStream? =
            context.contentResolver.openInputStream(selectedImage)
        val path = selectedImage.path
        val  ei: ExifInterface = when {
            Build.VERSION.SDK_INT > 23 && input != null ->
                ExifInterface(input)
            path != null -> ExifInterface(path)
            else -> null
        } ?: return img
        return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90.0f) ?: img
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180.0f) ?: img
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270.0f) ?: img
            else -> img
        }
    }
}

//method that reads an image from a Uri input stream
fun decodeUriStreamToSize(
    uri: Uri,
    width: Int,
    height: Int,
    context: Context
): Bitmap? {
    var inputStream: InputStream? = null
    try {
        val options: BitmapFactory.Options
        inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {
            options = BitmapFactory.Options()
            options.inJustDecodeBounds = false
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                options.inSampleSize = calculateInSampleSize(
                    options.outWidth, options.outHeight,
                    width, height)
                options.inJustDecodeBounds = false
                val bitmap = BitmapFactory.decodeStream(
                    inputStream, null, options)
                inputStream.close()
                return bitmap
            }
        }
        return null
    } catch (e: Exception) {
        return null
    } finally {
        inputStream?.close()
    }
}