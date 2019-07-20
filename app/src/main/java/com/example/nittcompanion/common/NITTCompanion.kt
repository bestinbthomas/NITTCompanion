package com.example.nittcompanion.notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.nittcompanion.common.NOTIFICATION_CHANNEL_REMINDER_ID

class NotificationChannel : Application() {

    override fun onCreate() {
        super.onCreate()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val reminderChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_REMINDER_ID,
                "Reminder for Events",
                NotificationManager.IMPORTANCE_HIGH
            )
            reminderChannel.description = "Receive reminders to events"

            getSystemService(NotificationManager::class.java).createNotificationChannel(reminderChannel)
        }
    }
}