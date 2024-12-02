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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
fun MicRecord(
    spokenTextState: MutableState<String>,
    isRecordingState: MutableState<Boolean>
) {
    val context = LocalContext.current

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ComponentActivity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.get(0) ?: "No speech detected"
            spokenTextState.value = spokenText
            isRecordingState.value = false
        } else {
            spokenTextState.value = "Didn't catch that. Please try again."
        }
    }

    if (spokenTextState.value == "Press the microphone to speak" || spokenTextState.value.isEmpty()) {
        // Show button with microphone icon and placeholder text
        Button(
            onClick = {
                isRecordingState.value = true
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
            modifier = Modifier.size(258.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = R.drawable.mic),
                    contentDescription = "Microphone",
                    modifier = Modifier.size(96.dp),
                    tint = Color.Unspecified
                )
                Text(
                    text = "Press the microphone to speak",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.Black
                )
            }
        }
    } else {
        // Replace button with spoken text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = spokenTextState.value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}





fun fetchEmotion(inputText: String, onResult: (List<HuggingFaceResponse>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d("HuggingFace", "Sending text to Hugging Face API: $inputText")
            Log.d("HuggingFace", "API Key: ${BuildConfig.HUGGINGFACE_API_KEY}")


            val response = RetrofitInstance.huggingFaceAPI.analyzeEmotion(
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

