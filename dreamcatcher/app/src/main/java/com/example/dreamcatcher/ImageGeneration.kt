package com.example.dreamcatcher

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.dreamcatcher.network.RetrofitInstance
import com.example.dreamcatcher.network.ImageRequest
import kotlinx.coroutines.launch


@Composable
fun ImageGeneration(prompt: String, onImageGenerated: (String) -> Unit) {
    val context = LocalContext.current
    val isLoading = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(prompt) {
        isLoading.value = true
        scope.launch {
            try {
                val response = RetrofitInstance.openAIImageAPI.generateImage(
                    ImageRequest(prompt = prompt, n = 1, size = "512x512")
                )
                if (response.data.isNotEmpty()) {
                    onImageGenerated(response.data[0].url)
                } else {
                    Toast.makeText(context, "No image generated", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading.value = false
            }
        }
    }
}
