package com.example.dreamcatcher.tools

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
import com.example.dreamcatcher.screens.moodColors

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
    Log.d("BarChart", "Mood Data: $moodData")
    val moodHierarchy = listOf(
        "joy", "surprise", "neutral", "sadness", "anger", "disgust", "fear"
    )
    val sortedMood = moodHierarchy.mapNotNull { mood ->
        moodData[mood]?.let { mood to it }
    }

    val maxScore = moodData.values.maxOrNull() ?: 1f
    val yAxisMarker = 5
//    val barWidth = 40.dp
    val markerStep = maxScore / yAxisMarker

    Canvas(modifier = modifier) {
        val totalBars = sortedMood.size
        val barSpacingRatio = 0.2f
        val barWidth = size.width / (totalBars + (totalBars - 1) * barSpacingRatio)
        val barSpacing = barWidth * barSpacingRatio

        sortedMood.forEachIndexed { index, (label, score) ->
            val barHeight = (score / maxScore) * size.height
            val xOffset = index * (barWidth + barSpacing)
            val barColor = moodColors[label] ?: Color.Gray

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
fun PieChart(
    moodData: Map<String, Float>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (moodData.isEmpty()) return@Canvas
        val total = moodData.values.sum()

        var startAngle = 0f

        moodData.forEach { (label, value) ->
            val sweepAngle = (value / total) * 360f
            val color = moodColors[label] ?: Color.Gray

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            startAngle += sweepAngle
        }
    }
}
