package com.example.dreamcatcher.screens

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dreamcatcher.MicRecord
import com.example.dreamcatcher.R

@Composable
fun TodayScreen() {
    val spokenTextState = remember { mutableStateOf("Press the microphone to speak") }
    val isRecordingState = remember { mutableStateOf(false) }
    val isEditingState = remember { mutableStateOf(false) }

    val icons = listOf(
        "Accept" to R.drawable.accept,
        "Regenerate" to R.drawable.regenerate,
        "Edit" to R.drawable.edit
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text("Image Placeholder", color = MaterialTheme.colorScheme.onPrimary)
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
                description = "Accept",
                label = "Accept",
                onClick = { /* Perform Accept action */ }
            )
            IconButton(
                iconRes = icons[1].second,
                description = "Regenerate",
                label = "Regenerate",
                onClick = { /* Perform Regenerate action */ }
            )
            IconButton(
                iconRes = icons[2].second,
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
            MicRecord(
                spokenTextState = spokenTextState,
                isRecordingState = isRecordingState
            )
        }

        if (isEditingState.value){
            EditDialog(
                spokenText = spokenTextState.value,
                onDismiss = { isEditingState.value = false },
                onSave = { updateText -> spokenTextState.value = updateText
                    isEditingState.value = false
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
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = description,
            modifier = Modifier.size(size),
            tint = Color.Unspecified
        )

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
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
        title = { Text("Edit Text") },
        text = {
            OutlinedTextField(
                value = editText,
                onValueChange = { editText = it },
                label = { Text("Your Sentence") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSave(editText) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
