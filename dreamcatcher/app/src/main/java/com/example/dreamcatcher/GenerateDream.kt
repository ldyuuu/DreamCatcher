package com.example.dreamcatcher

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

fun generateTestDreams(viewModel: MainViewModel, userId: Int) {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDate = System.currentTimeMillis()

    repeat(60) { index ->
        // Random date within the last 60 days
        val randomOffset = Random.nextLong(0, 60 * 24 * 60 * 60 * 1000L)
        val randomDate = currentDate - randomOffset
        val formattedDate = formatter.format(Date(randomDate))

        // Generate random moods that add up to 100
        val labels = listOf("joy", "surprise", "neutral", "sadness", "anger", "disgust", "fear")
        val moodScores = generateRandomMoodScores(labels.size)

        val moodJson = labels.zip(moodScores).map { (label, score) ->
            """{"label": "$label", "score": ${score / 100.0}}"""
        }.joinToString(prefix = "[", postfix = "]", separator = ",")

        // Create a test dream
        val dream = Dream(
            userId = userId,
            content = "Dream #$index content.",
            aiImageURL = "https://example.com/image$index.jpg",
            createdAt = randomDate,
            mood = moodJson,
            title = "Dream Title #$index"
        )

        // Add dream to the database using the ViewModel
        viewModel.addDream(dream)
    }
}

fun generateRandomMoodScores(count: Int): List<Int> {
    val scores = MutableList(count) { Random.nextInt(0, 100) }
    val total = scores.sum()
    return scores.map { it * 100 / total }
}
