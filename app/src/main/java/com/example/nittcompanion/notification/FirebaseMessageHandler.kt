package com.example.nittcompanion.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.nittcompanion.R
import com.example.nittcompanion.common.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class FirebaseMessageHandler : FirebaseMessagingService() {
    val TAG = "FireMessage"
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Log.d(TAG, "From: ${remoteMessage?.from}")

        // Check if message contains a data payload.
        remoteMessage?.data?.isNotEmpty()?.let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            sendNotification(remoteMessage.data)
        }

        // Check if message contains a notification payload.
        remoteMessage?.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

    }

    private fun sendNotification(data: Map<String, String>) {
        val tittle = data[KEY_TITTLE]
        val message = data[KEY_MESSAGE]
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(KEY_COURSE_ID, data[KEY_COURSE_ID])
        intent.putExtra(KEY_COURSE_NAME, data[KEY_COURSE_NAME])
        intent.putExtra(KEY_UPDATE_TYPE,data[KEY_UPDATE_TYPE])
        val pendingIntent =
            PendingIntent.getActivity(applicationContext, Calendar.getInstance().timeInMillis.toInt() % 1000, intent, 0)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_FCM_ID)
            .setSmallIcon(R.drawable.ic_noti_icon)
            .setContentTitle(tittle)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATION_NOTIFICATION_FCM_ID, notification)

    }
}