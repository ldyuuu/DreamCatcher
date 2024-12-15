package com.example.dreamcatcher.tools

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DreamReminderWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val channelId = "dream_reminder_channel"

        // 确保通知渠道存在
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Dream Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminder to log your dreams."
            }
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // 构建通知
        val notification = NotificationCompat.Builder(applicationContext, channelId)
           // .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Log Your Dream")
            .setContentText("Don't forget to log today's dream!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // 显示通知
        notificationManager.notify(1, notification)
    }
}
