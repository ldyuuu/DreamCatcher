package com.example.dreamcatcher.tools

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

import com.example.dreamcatcher.R

class ReminderReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "daily_reminder_channel"
        const val NOTIFICATION_ID = 100
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Alarm triggered at ${System.currentTimeMillis()}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("ReminderReceiver", "Notification permission not granted")
            return
        }

        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.dream)
            .setContentTitle("Daily Dream Reminder")
            .setContentText("Don't forget to log your dream today!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.e("ReminderReceiver", "Failed to send notification: ${e.message}")
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminder notifications"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
