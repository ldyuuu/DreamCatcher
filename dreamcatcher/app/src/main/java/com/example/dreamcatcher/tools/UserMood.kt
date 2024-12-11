package com.example.dreamcatcher.tools

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dreamcatcher.Dream
import com.example.dreamcatcher.R
import com.google.gson.Gson
import java.text.SimpleDateFormat
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
        val gson = Gson()
        val jsonArray = gson.fromJson(moodJson, com.google.gson.JsonArray::class.java)

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



fun parseMoodJson(moodJson: String): List<Pair<String, Float>> {
    return try {
        val gson = Gson()
        val jsonArray = gson.fromJson(moodJson, List::class.java) as List<Map<String, Any>>
        jsonArray.map {
            val label = it["label"] as String
            val score = (it["score"] as Double).toFloat()
            label to score
        }
    } catch (e: Exception) {
        emptyList()
    }
}


fun aggregateMoodData(dreams: List<Dream>, days: Int? = null): Map<String, Float> {
    val filteredDreams = if (days != null) {
        val cutoffTime = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L
        dreams.filter { it.createdAt >= cutoffTime }
    } else {
        dreams
    }

    val moodScores = mutableMapOf<String, MutableList<Float>>()

    filteredDreams.forEach { dream ->
        val moods = parseMoodJson(dream.mood)
        moods.forEach { (label, score) ->
            moodScores.computeIfAbsent(label) { mutableListOf() }.add(score)
        }
    }

    return moodScores.mapValues { (_, scores) -> scores.average().toFloat() }
}



fun getTopMoodForToday(dreams: List<Dream>): Pair<String, Float>? {
    val today = System.currentTimeMillis()
    val todayDreams = dreams.filter { dream ->
        val dreamDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dream.createdAt)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today)
        dreamDate == currentDate
    }

    if (todayDreams.isEmpty()) return null

    val aggregatedMood = mutableMapOf<String, MutableList<Float>>()
    todayDreams.forEach { dream ->
        val moods = parseMoodJson(dream.mood)
        moods.forEach { (label, score) ->
            aggregatedMood.computeIfAbsent(label) { mutableListOf() }.add(score)
        }
    }

    val averagedMood = aggregatedMood.mapValues { (_, scores) -> scores.average().toFloat() }
    val topMoodEntry = averagedMood.maxByOrNull { it.value }
    return topMoodEntry?.toPair()
}


fun evaluateMood(moods: Map<String, Float>): Pair<String,Boolean>{
    val negativeMoods = listOf("sadness", "anger", "disgust", "fear")
    val totalNegativeScore = moods.filterKeys { it in negativeMoods }.values.sum()
    val totalScore = moods.values.sum()

    val negativePercentage = if (totalScore > 0) {
        totalNegativeScore / totalScore * 100
    } else {
        0f
    }

    return if (negativePercentage > 60f){
        "Based on your mood trends over the last 14 days, it seems like you've been feeling low. It might help to talk to a mental health professional for support. Would you like assistance in finding one?" to false
    }else{
        "Great job! Over the past 14 days, your mood has been positive. Keep up the good energy!" to true
    }
}

@Composable
fun MoodStatusCard(
    moods: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    Log.d("MoodStatusCard", "Mood: $moods")
    val (message, isPositive) = evaluateMood(moods)

    Card(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

        }
    }
}


@Preview(showBackground = true)
@Composable
fun MoodStatusCardPreview() {
    val sampleMoods = mapOf(
        "fear" to 0.16f,
        "sadness" to 0.12f,
        "joy" to 0.18f,
        "neutral" to 0.13f,
        "surprise" to 0.14f,
        "anger" to 0.13f,
        "disgust" to 0.1f
    )

    MoodStatusCard(
        moods = sampleMoods,
    )
}
