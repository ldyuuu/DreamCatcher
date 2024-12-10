package com.example.dreamcatcher.tools

import androidx.compose.foundation.Image
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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




fun formatMoodWithIcons(
    moodJson: String,
    moodIcons: Map<String, Int>
): List<Pair<Int, String>> {
    return try {
        // Parse the JSON string into a JsonArray
        val gson = com.google.gson.Gson()
        val jsonArray = gson.fromJson(moodJson, com.google.gson.JsonArray::class.java)

        // Process each JsonObject and associate the icon with the formatted text
        jsonArray.mapNotNull { element ->
            val label = element.asJsonObject["label"].asString
            val score = (element.asJsonObject["score"].asFloat * 100).toInt()
            val iconRes = moodIcons[label]
            iconRes?.let { it to "$label: $score%" }
        }
            .sortedByDescending { it.second.split(": ")[1].replace("%", "").toInt() }
            .take(4)
    } catch (e: Exception) {
        emptyList()
    }
}


@Composable
fun MoodDisplayWithIcons(
    moods: List<Pair<Int, String>>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        moods.forEach { (iconRes, text) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                // Icon
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = text,
                    modifier = Modifier.size(36.dp)
                )
                // Text Label
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}


