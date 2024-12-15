package com.example.dreamcatcher.screens

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.TimePicker
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dreamcatcher.MainViewModel
import java.util.Calendar


@Composable
fun NotificationSettingScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val selectedHour by viewModel.selectedHour.collectAsState()
    val selectedMinute by viewModel.selectedMinute.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "Daily Dream Reminder",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 描述
        Text(
            text = "Set a daily reminder to log your dream. This reminder will help you build the habit of tracking your dreams consistently.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 当前设置时间
        Text(
            text = "Current Reminder Time: ${String.format("%02d", selectedHour)}:${String.format("%02d", selectedMinute)}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 占用剩余空间，将按钮推到底部
        Spacer(modifier = Modifier.weight(1f))

        // 底部按钮行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 返回按钮
            Button(
                onClick = { onBack() },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text(text = "Back")
            }

            // 设置时间按钮
            Button(
                onClick = {
                    showTimePickerDialog(
                        context = context,
                        initialHour = selectedHour,
                        initialMinute = selectedMinute
                    ) { hour, minute ->
                        // 保存设置的时间
                        if (checkExactAlarmPermission(context)) {
                            viewModel.scheduleNotification(context, hour, minute)
                        } else {
                            Toast.makeText(context, "Exact Alarm Permission is required", Toast.LENGTH_SHORT).show()
                            requestExactAlarmPermission(context)
                        }
                        viewModel.saveReminderTime(hour, minute)


                        viewModel.scheduleNotification(context, hour, minute)

                        // 提示用户已设置提醒时间
                        Toast.makeText(
                            context,
                            "Reminder set for ${String.format("%02d", hour)}:${String.format("%02d", minute)}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(text = "Set Time")
            }
        }
    }
}


fun showTimePickerDialog(
    context: Context,
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            onTimeSelected(hour, minute)
        },
        initialHour,
        initialMinute,
        true // 24-hour format
    )
    timePickerDialog.show()
}
private fun checkExactAlarmPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }
    return true
}

private fun requestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent().apply {
            action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}