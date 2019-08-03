package com.example.nittcompanion.common

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp

class NITTCompanion : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val eventChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_EVENT_ID,
                "Reminder for Events",
                NotificationManager.IMPORTANCE_HIGH
            )
            eventChannel.description = "Receive reminders to important events"

            val classChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_CLASS_ID,
                "Reminder for Attendance",
                NotificationManager.IMPORTANCE_HIGH
            )
            classChannel.description = "Remind to update attendance after each class"

            val updateChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_FCM_ID,
                "Reminder for Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            classChannel.description = "Reminds about new notes, Tests , Assignments"

            getSystemService(NotificationManager::class.java).createNotificationChannels(mutableListOf(eventChannel,classChannel,updateChannel))
        }
    }
}