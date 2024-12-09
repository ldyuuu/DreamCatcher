package com.example.dreamcatcher.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.dreamcatcher.Dream
import com.example.dreamcatcher.MainViewModel
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
fun DreamDetailScreen(
    viewModel: MainViewModel,
    userId: Int,
    date: String,
    onBack: () -> Unit
) {
    val dreams by viewModel.getDreamsByUserAndDate(userId, date).observeAsState(emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        // 返回按钮
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            IconButton(
                iconRes = R.drawable.back,
                description = "Back",
                label = "",
                onClick = onBack
            )
        }

        // 显示梦境信息
        if (dreams.isNotEmpty()) {
            LazyDreamInfoView(
                dreams = dreams,
                moodIcons = moodIcons,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp)
            )
        } else {
            Text(
                text = "No dreams for $date",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }


//        Button(onClick = {
//            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            val date = formatter.parse("2024-12-1") //
//            val timestamp = date?.time ?: System.currentTimeMillis()
//            val dream = Dream(
//                userId = 1,
//                content = "A dream about big carrot.",
//                aiImageURL = "https://example.com/image.jpg",
//                createdAt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                    .parse("2024-12-01")?.time ?: System.currentTimeMillis(),
//                mood = "Anger",
//                title="nothing"
//            )
//            viewModel.addDream(dream)
//        }) {
//            Text("Insert Dream")
//        }
    }
}

@Composable
fun LazyDreamInfoView(dreams: List<Dream>, moodIcons: Map<String, Int>, modifier: Modifier = Modifier) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(dreams.size) { index ->
            val dream = dreams[index]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // 显示图片
                AsyncImage(
                    model = dream.aiImageURL,
                    contentDescription = "Dream Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                )

                val formattedDate = formatTimestamp(dream.createdAt)

                Spacer(modifier = Modifier.height(16.dp))

                // 显示梦境信息
                Text(text = "Date: $formattedDate", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Dream: ${dream.content}",
                    style = MaterialTheme.typography.bodyMedium
                )
                val moodWithIcons = formatMoodWithIcons(dream.mood, moodIcons)
                MoodDisplayWithIcons(moods = moodWithIcons)

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(date)
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
