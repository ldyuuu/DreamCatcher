package com.example.dreamcatcher.tools

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import com.example.dreamcatcher.BuildConfig
import com.example.dreamcatcher.Dream
import com.example.dreamcatcher.R
import com.example.dreamcatcher.network.HuggingFaceRequest
import com.example.dreamcatcher.network.HuggingFaceResponse
import com.example.dreamcatcher.network.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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


/**
 * Dynamically resolved mood icons based on the mood label.
 */
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

/**
 * Format the mood JSON string into a list of mood labels and scores,
 * and sort them by score, return the top 4 moods.
 */
fun formatMood(
    moodJson: String
): List<Pair<String, Int>> {
    return try {
        val gson = Gson()
        val jsonArray = gson.fromJson(moodJson, com.google.gson.JsonArray::class.java)

        jsonArray.mapNotNull { element ->
            val label = element.asJsonObject["label"].asString
            val score = (element.asJsonObject["score"].asFloat * 100).toInt()
            label to score
        }.sortedByDescending { it.second }.take(4)
    } catch (e: Exception) {
        emptyList()
    }
}


/**
 * Parse the mood JSON string into a list of mood labels and scores.
 */
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


/**
 * Return n days of mood data from the given list of dreams.
 */
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


/**
 * Return the top mood for today, if there are multiple dreams, return the top after calculate avg
 */
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


/**
 * Calculate the percentage of negative moods in the given mood data, and return a
 * message based on the result.
 */
fun evaluateMood(moods: Map<String, Float>): Pair<String, Boolean> {
    val negativeMoods = listOf("sadness", "anger", "disgust", "fear")
    val totalNegativeScore = moods.filterKeys { it in negativeMoods }.values.sum()
    val totalScore = moods.values.sum()

    val negativePercentage = if (totalScore > 0) {
        totalNegativeScore / totalScore * 100
    } else {
        0f
    }

    return if (negativePercentage > 50f) {
        "Based on your mood trends over the last 14 days, it seems like you've been feeling low. " +
                "It might help to talk to a mental health professional for support. Use Map to find " +
                "someone to talk to nearby" to false
    } else {
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

suspend fun fetchEmotion(inputText: String): List<HuggingFaceResponse> {
    return withContext(Dispatchers.IO) {
        try {
            Log.d("HuggingFace", "Sending text to Hugging Face API: $inputText")
            Log.d("HuggingFace", "API Key: ${BuildConfig.HUGGINGFACE_API_KEY}")

            val response = RetrofitInstance.huggingFaceAPI.analyzeEmotion(
                request = HuggingFaceRequest(inputs = inputText)
            )

            val flattenedResponse = response.flatten() // Clean up response
            Log.d("HuggingFace", "Hugging Face Response: $response")
            flattenedResponse
        } catch (e: Exception) {
            Log.e("HuggingFace", "Error during Hugging Face API request: ${e.message}")
            emptyList()
        }
    }
}

