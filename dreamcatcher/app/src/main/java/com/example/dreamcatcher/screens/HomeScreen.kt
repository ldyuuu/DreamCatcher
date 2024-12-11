package com.example.dreamcatcher.screens

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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import com.example.dreamcatcher.MainViewModel


@Composable
fun HomeScreen(viewModel: MainViewModel,navController: NavHostController) {
    // Observing dreams for the logged-in user
    val dreams = viewModel.getDreamsForLoggedInUser().observeAsState(emptyList()).value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Top Section
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(5) { index ->
                    when (index) {
                        0 -> InfoCardWithLink(
                            title = "Enter Today's Dream",
                            description = "Log your dream for today",
                            navController = navController,
                            destination = "today",
                            backgroundResId = R.drawable.today_card_background
                        )
                        1 -> InfoCardWithLink(
                            title = "View Dream Calendar",
                            description = "Check your dream logs",
                            navController = navController,
                            destination = "calendar",
                            backgroundResId = R.drawable.calendar_card_background
                        )
//                        2 -> InfoCardWithLink(
//                            title = "Settings",
//                            description = "Adjust your preferences",
//                            navController = navController,
//                            destination = "settings",
//                            backgroundResId = R.drawable.settings_card_background
//                        )
                        2 ->InfoCardWithLink(
                                title = "Find Nearby Therapists",
                                description = "Locate therapists near your location",
                                navController = navController,
                                destination = "map",
                                backgroundResId = R.drawable.map_card_background
                            )


                    }
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
                            moodData = aggregateMoodData(dreams, days = 7),
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
                            moodData = aggregateMoodData(dreams, days = 30),
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
fun InfoCard(title: String, description: String, cardWithFraction: Float = 1f) {
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWith * cardWithFraction

    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(220.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
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


@Composable
fun AggregatedMoodDisplay(moodData: Map<String, Float>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        moodData.forEach { (mood, averageScore) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = moodIcons[mood] ?: R.drawable.neutral),
                    contentDescription = mood,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "$mood: ${"%.1f".format(averageScore * 100)}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun aggregateMoodsByDate(dreams: List<Dream>): Map<String, Map<String, Float>> {
    val dateGroupedData = dreams.groupBy { dream ->
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dream.createdAt)
    }

    return dateGroupedData.mapValues { (_, dreamsOnDate) ->
        aggregateMoodData(dreamsOnDate)
    }
}


@Composable
fun BarChart(
    moodData: Map<String, Float>,
    barColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .padding(16.dp)
) {
    val moodHierarchy = listOf(
        "joy", "surprise", "neutral", "sadness", "anger", "disgust", "fear"
    )
    val sortedMood = moodHierarchy.mapNotNull { mood ->
        moodData[mood]?.let { mood to it }
    }

    val maxScore = moodData.values.maxOrNull() ?: 1f
//    val barWidth = 40.dp

    Canvas(modifier = modifier) {
        val totalBars = sortedMood.size
        val barSpacingRatio = 0.2f // Space as a fraction of bar width
        val barWidth = size.width / (totalBars + (totalBars - 1) * barSpacingRatio)
        val barSpacing = barWidth * barSpacingRatio

        sortedMood.forEachIndexed { index, (label, score) ->
            val barHeight = (score / maxScore) * size.height
            val xOffset = index * (barWidth + barSpacing)

            // Draw Bar
            drawRect(
                color = barColor,
                topLeft = Offset(x = xOffset, y = size.height - barHeight),
                size = Size(width = barWidth, height = barHeight)
            )

            // Draw Label
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    label,
                    xOffset + barWidth / 2,
                    size.height + 16.dp.toPx(),
                    android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 12.dp.toPx()
                        color = textColor.toArgb()
                    }
                )
            }
        }
    }
}

@Composable
fun InfoCardWithLink(
    title: String,
    description: String,
    navController: NavHostController,
    destination: String,
    backgroundResId: Int
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth * 0.8f

    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(220.dp)
            .padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        onClick = { navController.navigate(destination) }
    ) {
        Box {
            Image(
                painter = painterResource(id = backgroundResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f), // Shadow offset
                            blurRadius = 4f
                        )
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {

}