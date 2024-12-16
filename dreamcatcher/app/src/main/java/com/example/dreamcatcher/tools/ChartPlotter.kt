package com.example.dreamcatcher.tools

import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

val moodColors = mapOf(
    "joy" to Color(0xFFF2D923), // Yellow
    "sadness" to Color(0xFF2196F3), // Blue
    "anger" to Color(0xFFF44336), // Red
    "neutral" to Color(0xFF7BF73E), // Green
    "surprise" to Color(0xFFFF9800), // Orange
    "disgust" to Color(0xFFED7F4A), // Dark red
    "fear" to Color(0xFF673AB7) // Purple
)

/**
 * Bar chart
 */
@Composable
fun BarChart(
    moodData: Map<String, Float>,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .padding(16.dp)
) {
    Log.d("BarChart", "Mood Data: $moodData")
    val moodOrder = listOf(
        "joy", "surprise", "neutral", "sadness", "anger", "disgust", "fear"
    )
    val sortedMood = moodOrder.mapNotNull { mood ->
        moodData[mood]?.let { mood to it }
    }
    val maxScore = moodData.values.maxOrNull() ?: 1f

    Canvas(modifier = modifier) {
        val numBar = sortedMood.size
        val barSpaceRatio = 0.2f
        val barWidth = size.width / (numBar + (numBar - 1) * barSpaceRatio)
        val barSpacing = barWidth * barSpaceRatio

        sortedMood.forEachIndexed { index, (label, score) ->
            val barHeight = (score / maxScore) * size.height
            val xOffset = index * (barWidth + barSpacing)
            val barColor = moodColors[label] ?: Color.Gray

            // Draw Rectangular Bar
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
                    Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 12.dp.toPx()
                        color = textColor.toArgb()
                    }
                )
            }
        }
    }
}


/**
 * Pie chart
 */
@Composable
fun PieChart(
    moodData: Map<String, Float>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (moodData.isEmpty()) return@Canvas
        val total = moodData.values.sum()

        val diameter = minOf(size.width, size.height)
        val chartSize = Size(diameter, diameter)
        var startAngle = 0f

        moodData.forEach { (label, value) ->
            val sweepAngle = (value / total) * 360f
            val color = moodColors[label] ?: Color.Gray

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true, // auto determined center of the pie
                topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                ),
                size = chartSize
            )
            startAngle += sweepAngle
        }
    }
}
