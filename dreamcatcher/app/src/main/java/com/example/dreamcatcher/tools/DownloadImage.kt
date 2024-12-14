package com.example.dreamcatcher.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL


/**
 * Download an image from a URL and save it to a file in the app's internal storage, return the
 * absolute path of the image file
 */
suspend fun downloadImage(context: Context, imageUrl: String, fileName: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)

            // Open connection to the URL and get the input stream for image data
            val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            // Define the file in local storage to save the image
            val file = File(context.filesDir, fileName)
            Log.d("downloadImage", "Saving to file: ${file.absolutePath}")

            val outputStream = FileOutputStream(file)
            //Compose into PNG
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            Log.d("downloadImage", "Image saved successfully: ${file.absolutePath}")
            // Return the absolute path of the image file
            file.absolutePath
        } catch (e: Exception) {
            Log.e("downloadImage", "Error downloading image: ${e.message}")
            null
        }
    }
}
