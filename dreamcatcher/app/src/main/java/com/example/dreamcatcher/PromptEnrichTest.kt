package com.example.dreamcatcher

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.dreamcatcher.network.HuggingFaceAPI
import com.example.dreamcatcher.network.HuggingFaceRequest
import com.example.dreamcatcher.network.HuggingFaceResponse
import com.example.dreamcatcher.network.RetrofitInstance
import com.example.dreamcatcher.network.ImageRequest
import kotlinx.coroutines.launch

@Composable
fun PromptEnrichTest(navController: NavHostController) {
    val prompt = "A flying pig chasing a rabbit"
    val scope = rememberCoroutineScope()

    val enrichedPrompt = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Original Prompt: $prompt")

        Button(
            onClick = {
                isLoading.value = true
                scope.launch {
                    try {
                        val enriched = enrichPrompt(prompt, RetrofitInstance.huggingFaceAPI)
                        enrichedPrompt.value = enriched
                    } catch (e: Exception) {
                        Log.e("EnrichmentError", "Error enriching prompt: ${e.message}")
                    } finally {
                        isLoading.value = false
                    }
                }
            },
            enabled = !isLoading.value
        ) {
            Text(if (isLoading.value) "Processing ..." else "Enrich")
        }

        Text("Enriched Prompt: ${enrichedPrompt.value}")
    }
}

suspend fun enrichPrompt(
    simplePrompt: String,
    huggingFaceApi: HuggingFaceAPI,
): String {
    val enrichedPromptResponse = huggingFaceApi.enrichPrompt(
        HuggingFaceRequest(
            inputs = """
                The following prompt should be enriched with vivid, imaginative, and visually descriptive details suitable for an artistic image:
                
                Original: $simplePrompt
                Enriched:
            """.trimIndent()
        )
    )
    return enrichedPromptResponse.firstOrNull()?.generated_text ?: simplePrompt
}
