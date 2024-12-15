package com.example.dreamcatcher.tools

import android.Manifest
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
import com.example.dreamcatcher.R


@Composable
fun MicRecord(
    spokenTextState: MutableState<String>,
    isRecording: MutableState<Boolean>
) {
    val context = LocalContext.current

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ComponentActivity.RESULT_OK && result.data != null) {
            val speechResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = speechResults?.get(0) ?: "No speech detected"
            spokenTextState.value = spokenText
            isRecording.value = false
        } else {
            spokenTextState.value = "Didn't catch that. Please try again."
        }
    }

    if (spokenTextState.value == "Press the microphone to speak"
        || spokenTextState.value.isEmpty()
        || spokenTextState.value == "Didn't catch that. Please try again.") {
        // Show button with microphone icon and placeholder text
        Button(
            onClick = {
                isRecording.value = true
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    try {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL, // conversational speech
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM // casual phrases
                            )
                            // Add specific information
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                        }
                        speechRecognizerLauncher.launch(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            "Speech recognition not supported",
                            Toast.LENGTH_SHORT
                        ).show()
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
                    color = MaterialTheme.colorScheme.onBackground
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


