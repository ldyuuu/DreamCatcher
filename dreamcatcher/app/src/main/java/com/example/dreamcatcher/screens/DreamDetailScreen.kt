package com.example.dreamcatcher.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

@OptIn(ExperimentalMaterial3Api::class)
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


        // 显示梦境信息
        if (dreams.isNotEmpty()) {
            LazyDreamInfoView(dreams = dreams)
        } else {
            Text(
                text = "No dreams for $date",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .absoluteOffset(x = 16.dp, y = 16.dp)

            //contentAlignment = Alignment.CenterStart
        ) {
            IconButton(
                iconRes = R.drawable.back,
                description = "Go Back",
                label = "",
                onClick = onBack
            )
        }
    }
}

@Composable
fun LazyDreamInfoView(dreams: List<Dream>) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
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
                Text(text = "Content: ${dream.content}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Mood: ${dream.mood}", style = MaterialTheme.typography.bodyMedium)

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
