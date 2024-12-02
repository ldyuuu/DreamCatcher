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
fun ImageGenerateTest() {
    val context = LocalContext.current
    val fixedPrompt = "A whimsical flying pig with glittering silver wings soaring above a dreamy sunset landscape filled with fluffy pink clouds. The pig is gracefully gliding through the sky, its wings shimmering in the golden and pink hues of the sunset. The landscape below features rolling hills faintly visible beneath the glowing pink clouds, adding a serene and magical atmosphere." // Fixed input for testing
    val generatedImageUrl = remember { mutableStateOf<String?>(null) }
    val isLoading = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Fixed Prompt Display
        Text(
            text = "Generating image for prompt: \"$fixedPrompt\"",
            modifier = Modifier.padding(8.dp)
        )

        // Generate Image Button
        Button(
            onClick = {
                isLoading.value = true
                scope.launch {
                    try {
                        val response = RetrofitInstance.openAIImageAPI.generateImage(
                            ImageRequest(prompt = fixedPrompt, n = 1, size = "512x512")
                        )
                        if (response.data.isNotEmpty()) {
                            generatedImageUrl.value = response.data[0].url
                        } else {
                            Toast.makeText(context, "No image generated", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading.value = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading.value
        ) {
            Text(if (isLoading.value) "Generating..." else "Generate Image")
        }

        // Display Generated Image
        generatedImageUrl.value?.let { imageUrl ->
            Text("Generated Image:", modifier = Modifier.padding(top = 16.dp))
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}
