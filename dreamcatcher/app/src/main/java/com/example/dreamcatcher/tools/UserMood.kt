package com.example.dreamcatcher.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dreamcatcher.R

val moodIcons = mapOf(
    "anger" to R.drawable.anger,
    "disgust" to R.drawable.disgust,
    "fear" to R.drawable.fear,
    "joy" to R.drawable.joy,
    "neutral" to R.drawable.neutral,
    "sadness" to R.drawable.sadness,
    "surprise" to R.drawable.surprise
)


@Composable
fun MoodDisplay(moods: List<Pair<String, Int>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        moods.forEach { (mood, percentage) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                // Icon
                Icon(
                    painter = painterResource(id = moodIcons[mood] ?: R.drawable.neutral),
                    contentDescription = mood,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Unspecified
                )
                // Text Label
                Text(
                    text = "$mood: $percentage%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}



