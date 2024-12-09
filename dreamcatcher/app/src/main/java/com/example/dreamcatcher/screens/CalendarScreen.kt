package com.example.dreamcatcher.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*



import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import java.util.Calendar
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.dreamcatcher.Dream
import com.example.dreamcatcher.MainViewModel
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
fun parseMood(jsonString: String): String {
    return try {
        val jsonArray = JSONArray(jsonString)
        val mood = jsonArray.getJSONObject(0) // Assuming the first mood is the most significant
        mood.getString("label")
    } catch (e: Exception) {
        "Unknown"
    }
}
@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    userId: Int,
    onDateSelected: (String) -> Unit
) {
    val loggedInUser by viewModel.loggedInUser.collectAsState()

    if (loggedInUser == null) {
        // 显示加载占位符
        Text("Loading user data...")
        return
    }

    val calendar = remember { Calendar.getInstance() }
    val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = 0)

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部星期标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Sun","Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 滚动的月份列表
        LazyColumn(
            state = scrollState,
            reverseLayout = true, // 反向布局，当前月份在底部
            modifier = Modifier.fillMaxSize()
        ) {
            items(24) { offset ->
                val monthCalendar = (calendar.clone() as Calendar).apply {
                    add(Calendar.MONTH, -offset)
                }
                val year = monthCalendar.get(Calendar.YEAR)
                val month = monthCalendar.get(Calendar.MONTH)

                MonthView(
                    viewModel = viewModel,
                    userId = userId,
                    year = year,
                    month = month,
                    onDateSelected = onDateSelected

                )
            }
        }

    }

    // 显示当前月份在页面底部
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        val visibleMonthIndex = scrollState.firstVisibleItemIndex
        val displayCalendar = (calendar.clone() as Calendar).apply {
            add(Calendar.MONTH, -visibleMonthIndex)
        }
        val displayYear = displayCalendar.get(Calendar.YEAR)
        val displayMonth = displayCalendar.get(Calendar.MONTH) + 1

        Text(
            text = "$displayYear-$displayMonth",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun MonthView(
    viewModel: MainViewModel,
    userId: Int,
    year: Int,
    month: Int,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 星期天 = 1

    val totalGridItems = daysInMonth + (firstDayOfWeek - 1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 月份标题
        Text(
            text = "$year-${month + 1}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 日期网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
                .heightIn(max = 400.dp)

        ) {
            items(totalGridItems) { index ->
                val day = index - (firstDayOfWeek - 2)
                if (day < 1 || day > daysInMonth) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(48.dp)
                    )
                } else {
                    val date = String.format("%04d-%02d-%02d", year, month + 1, day)

                    val dreams by viewModel.getDreamsByUserAndDate(userId, date).observeAsState(emptyList())

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(48.dp)
                            .clickable { onDateSelected(date) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (dreams.isNotEmpty()) {
                                val moodLabel = parseMood(dreams.firstOrNull()?.mood ?: "")
                                Text(
                                    text = moodLabel ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
