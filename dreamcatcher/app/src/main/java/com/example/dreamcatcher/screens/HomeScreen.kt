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
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.example.dreamcatcher.MainViewModel
import com.example.dreamcatcher.tools.BarChart
import com.example.dreamcatcher.tools.MoodStatusCard
import com.example.dreamcatcher.tools.PieChart
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
fun HomeScreen(viewModel: MainViewModel, navController: NavHostController) {
    // Observing dreams for the logged-in user
    val dreams = viewModel.getDreamsForLoggedInUser().observeAsState(emptyList()).value
    val topMood = getTopMoodForToday(dreams)
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardModifier = Modifier
        .width(screenWidth * 0.8f)
        .height(220.dp)
        .padding(8.dp)
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
                        1 -> InfoCardWithLink(
                            title = "Enter Today's Dream",
                            description = "Log your dream for today",
                            navController = navController,
                            destination = "today",
                            backgroundResId = R.drawable.today_card_background
                        )
                        2 -> InfoCardWithLink(
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
                        3 ->InfoCardWithLink(
                                title = "Find Nearby Therapists",
                                description = "Locate therapists near your location",
                                navController = navController,
                                destination = "map",
                                backgroundResId = R.drawable.map_card_background
                            )
                        0-> InfoCard(
                            topMood = topMood,
                            modifier = cardModifier,
                            allMoods = allMoods
                        )
                        4-> MoodStatusCard(
                            moods = fourteenDayMood,
                            modifier = cardModifier,
                            onTherapyClick = { /*TODO*/ },
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
                Spacer(modifier = Modifier.width(30.dp))
                // Pie Chart on the Left
                if (allMoods != null) {
                    PieChart(
                        moodData = allMoods,
                        modifier = Modifier
                            .weight(1f, fill = false)
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
                            offset = Offset(2f, 2f),
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