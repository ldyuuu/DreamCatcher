package com.example.dreamcatcher.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import com.example.dreamcatcher.Dream
import com.example.dreamcatcher.R
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.max
import com.example.dreamcatcher.MainViewModel
import com.example.dreamcatcher.tools.BarChart
import com.example.dreamcatcher.tools.MoodStatusCard
import com.example.dreamcatcher.tools.PieChart
import com.example.dreamcatcher.tools.aggregateMoodData
import com.example.dreamcatcher.tools.getTopMoodForToday


val moodColors = mapOf(
    "joy" to Color(0xFFF2D923), // Yellow
    "sadness" to Color(0xFF2196F3), // Blue
    "anger" to Color(0xFFF44336), // Red
    "neutral" to Color(0xFF7BF73E), // Green
    "surprise" to Color(0xFFFF9800), // Orange
    "disgust" to Color(0xFFED7F4A), // Dark red
    "fear" to Color(0xFF673AB7) // Purple
)


@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val cardModifier = Modifier
        .width(330.dp)
        .height(200.dp)

    val dreams by viewModel.dreams.collectAsState()
    val topMood = getTopMoodForToday(dreams)
    val allMoods = if (topMood != null) {
        aggregateMoodData(dreams.filter { dream ->
            val dreamDate =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dream.createdAt)
            val currentDate = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(System.currentTimeMillis())
            dreamDate == currentDate
        })
    } else null

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val sevenDayMood = aggregateMoodData(dreams, days = 7)
        val fourteenDayMood = aggregateMoodData(dreams, days = 14)
        val thirtyDayMood = aggregateMoodData(dreams, days = 30)

        Log.d("HomeScreen", "7-Day Mood: $sevenDayMood")
        Log.d("HomeScreen", "30-Day Mood: $thirtyDayMood")
        Log.d("HomeScreen", "Dreams: $dreams")

        // Top Section
        item {
            Text(
                text = "In Focus",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    InfoCard(
                        topMood = topMood,
                        modifier = cardModifier,
                        allMoods = allMoods
                    )
                }

                item {
                    MoodStatusCard(
                        moods = fourteenDayMood,
                        modifier = cardModifier,
                        onTherapyClick = { /*TODO*/ },
                    )
                }
            }
        }

        // Bottom Section
        item {
            Text(
                text = "At a Glance",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Graph placeholder
        item {
            Text(
                text = "7-Days Mood",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.elevatedCardElevation(8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    if (dreams.isNotEmpty()) {
                        BarChart(
                            moodData = sevenDayMood,
                            barColor = MaterialTheme.colorScheme.primary,
                            textColor = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        Text(
                            text = "No dreams available to display trends.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "30-Days Mood",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.elevatedCardElevation(8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    if (dreams.isNotEmpty()) {
                        BarChart(
                            moodData = thirtyDayMood,
                            barColor = MaterialTheme.colorScheme.primary,
                            textColor = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        Text(
                            text = "No dreams available to display trends.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }

    }
}


@Composable
fun InfoCard(
    topMood: Pair<String, Float>?,
    modifier: Modifier = Modifier,
    allMoods: Map<String, Float>?,
    cardWithFraction: Float = 0.85f
) {
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWith * cardWithFraction

    Card(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title at the Top-Left
            Text(
                text = "Today's Mood",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pie Chart on the Left
                if (allMoods != null) {
                    PieChart(
                        moodData = allMoods,
                        modifier = Modifier
                            .weight(1f)
                            .size(130.dp)
                            .padding(end = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Icon and Mood Info on the Right
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                ) {
                    if (topMood != null) {
                        Icon(
                            painter = painterResource(
                                id = moodIcons[topMood.first] ?: R.drawable.neutral
                            ),
                            contentDescription = topMood.first,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(bottom = 8.dp),
                            tint = Color.Unspecified
                        )
                        Text(
                            text = "${topMood.first.capitalize()}: ${"%.1f".format(topMood.second * 100)}%",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "Log your daily dream",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}



