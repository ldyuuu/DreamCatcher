package com.example.dreamcatcher.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.dreamcatcher.tools.ImageGeneration
import com.example.dreamcatcher.tools.MicRecord
import com.example.dreamcatcher.R


open class TodayViewModel : ViewModel() {
    val spokenTextState = mutableStateOf("Press the microphone to speak")
    val isRecordingState = mutableStateOf(false)

    fun updateSpokenText(newText: String) {
        spokenTextState.value = newText
    }

    fun setRecordingState(isRecording: Boolean) {
        isRecordingState.value = isRecording
    }
}


@Composable
fun TodayScreen(todayViewModel: TodayViewModel) {
    val spokenTextState = todayViewModel.spokenTextState
    val isRecordingState = todayViewModel.isRecordingState
    val generatedImageUrl = remember { mutableStateOf<String?>(null) }
    val isEditingState = remember { mutableStateOf(false) }
    val isGeneratingImage = remember { mutableStateOf(false) }

    val icons = listOf(
        "Paint" to R.drawable.paint,
        "Accept" to R.drawable.accept,
        "Regenerate" to R.drawable.regenerate,
        "Edit" to R.drawable.edit
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Image Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            generatedImageUrl.value?.let { imageUrl ->
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl),
                    contentDescription = "Dream Image",
                    modifier = Modifier
                        .size(360.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } ?: Text(
                "Press Accept to generate a dream image",
            )
        }

        // Middle Section with Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                iconRes = icons[0].second,
                description = "Paint",
                label = "Paint",
                onClick = { isGeneratingImage.value = true },
                enabled = spokenTextState.value.isNotEmpty() && spokenTextState.value != "Press the microphone to speak"
            )
            IconButton(
                iconRes = icons[1].second,
                description = "Accept",
                label = "Accept",
                onClick = { /* Perform Accept action */ }
            )
            IconButton(
                iconRes = icons[3].second,
                description = "Edit",
                label = "Edit",
                onClick = { isEditingState.value = true }
            )
        }


        // Bottom Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (isEditingState.value) {
                EditDialog(
                    spokenText = spokenTextState.value,
                    onDismiss = { isEditingState.value = false },
                    onSave = { updatedText ->
                        todayViewModel.updateSpokenText(updatedText)
                        isEditingState.value = false
                    }
                )
            } else {
                MicRecord(
                    spokenTextState = todayViewModel.spokenTextState,
                    isRecordingState = todayViewModel.isRecordingState
                )
            }
        }

        if (isGeneratingImage.value) {
            ImageGeneration(
                prompt = spokenTextState.value,
                onImageGenerated = { imageUrl ->
                    generatedImageUrl.value = imageUrl
                    isGeneratingImage.value = false
                }
            )
        }
    }
}


@Composable
fun IconButton(
    iconRes: Int,
    description: String,
    label: String,
    size: Dp = 48.dp,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .clickable(enabled = enabled) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = description,
            modifier = Modifier.size(size),
            tint = if (enabled) Color.Unspecified else Color.Gray
        )

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
            color = if (enabled) Color.Black else Color.Gray
        )
    }
}

@Composable
fun EditDialog(
    spokenText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var editText by remember { mutableStateOf(spokenText) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Edit Your Dream") },
        text = {
            OutlinedTextField(
                value = editText,
                onValueChange = { editText = it },
                label = { Text("Your Dream", color = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(editText) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Cancel")
            }
        }
    )
}
