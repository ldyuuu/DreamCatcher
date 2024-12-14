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

suspend fun downloadImage(context: Context, imageUrl: String, fileName: String): String? {
    Log.d("downloadImage", "Starting download: $imageUrl")
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            Log.d("downloadImage", "Connecting to URL: $imageUrl")

            val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            Log.d("downloadImage", "Bitmap created successfully")

            val file = File(context.filesDir, fileName)
            Log.d("downloadImage", "Saving to file: ${file.absolutePath}")

            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            Log.d("downloadImage", "Image saved successfully: ${file.absolutePath}")
            file.absolutePath // Return the file path
        } catch (e: Exception) {
            Log.e("downloadImage", "Error downloading image: ${e.message}")
            null
        }
    }
}


@Composable
fun DisplayGeneratedImage(imagePath: String) {
    if (File(imagePath).exists()) {
        Image(
            painter = rememberAsyncImagePainter(model = File(imagePath).toUri()),
            contentDescription = "Generated Image",
            modifier = Modifier.size(360.dp)
        )
    } else {
        Text("Image not found")
    }
}