package com.example.dreamcatcher

import android.Manifest
import android.util.Log
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController

import com.example.dreamcatcher.network.HuggingFaceRequest
import com.example.dreamcatcher.network.HuggingFaceResponse
import com.example.dreamcatcher.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MicTestScreen(navController: NavHostController) {
    val context = LocalContext.current
    val spokenTextState = remember { mutableStateOf("Press the microphone to speak") }
    val emotionResultState = remember { mutableStateOf("Waiting for emotion analysis...") }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ComponentActivity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.get(0) ?: "No speech detected"
            spokenTextState.value = spokenText

            emotionResultState.value = "Analyzing emotion..."
            fetchEmotion(spokenText) { emotionResponses ->
                if (emotionResponses.isNotEmpty()) {
                    val resultText = emotionResponses.joinToString("\n") {
                        "${it.label}: ${(it.score * 100).toInt()}%"
                    }
                    emotionResultState.value = resultText
                } else {
                    emotionResultState.value = "Failed to analyze emotion. Please try again."
                }
            }
        } else {
            spokenTextState.value = "Didn't catch that. Please try again."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = spokenTextState.value,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Text(
            text = emotionResultState.value,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    try {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                        }
                        speechRecognizerLauncher.launch(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "Speech recognition not supported", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    ActivityCompat.requestPermissions(
                        context as ComponentActivity,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        100
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Speak Now")
        }

        Button(
            onClick = { navController.navigate("main_menu") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Back to Main Menu")
        }
    }
}


fun fetchEmotion(inputText: String, onResult: (List<HuggingFaceResponse>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d("HuggingFace", "Sending text to Hugging Face API: $inputText")
            Log.d("HuggingFace", "API Key: ${BuildConfig.HUGGINGFACE_API_KEY}")


            val response = RetrofitInstance.huggingFaceAPI.analyzeEmotion(
//                authToken = "Bearer ${BuildConfig.HUGGINGFACE_API_KEY}",
                request = HuggingFaceRequest(inputs = inputText)
            )

            val flattenedResponse = response.flatten()
            Log.d("HuggingFace", "Hugging Face Response: $response")
            withContext(Dispatchers.Main) {
                onResult(flattenedResponse)
            }
        } catch (e: Exception) {
            Log.e("HuggingFace", "Error during Hugging Face API request: ${e.message}")
            withContext(Dispatchers.Main) {
                onResult(emptyList())
            }
        }
    }
}

