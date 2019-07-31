package com.example.nittcompanion.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.nittcompanion.R
import com.example.nittcompanion.common.*
import java.util.*

class NotifyAlert : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val eventID = intent?.getStringExtra(KEY_EVENT_ID)
        val eventName = intent?.getStringExtra(KEY_EVENT_NAME)
        val courseID = intent?.getStringExtra(KEY_COURSE_ID)
        val isClass = intent?.getBooleanExtra(KEY_IS_CLASS,false)

        val intentOnTouch = Intent(context, MainActivity::class.java)
        intentOnTouch.putExtra(KEY_EVENT_ID,eventID)
        intentOnTouch.putExtra(KEY_COURSE_ID,courseID)
        intentOnTouch.putExtra(KEY_IS_CLASS,isClass)
        val activityPIntent = PendingIntent.getActivity(context, REQ_NOTI_MAIN_ACTIVITY, intentOnTouch, PendingIntent.FLAG_UPDATE_CURRENT)


        Log.e("NotifyAlert","eventname : $eventName event id $eventID course id $courseID")

        if (isClass == true){
            Log.e("NotifyAlert","Notify for class called")
            val yesIntent = Intent(context, NotificationBroadCastReceiver::class.java)
            val noIntent = Intent(context, NotificationBroadCastReceiver::class.java)
            val cancelIntent = Intent(context, NotificationBroadCastReceiver::class.java)

            yesIntent.putExtra(KEY_EVENT_ID, eventID)
            yesIntent.putExtra(KEY_COURSE_ID, courseID)
            yesIntent.putExtra(KEY_ACTION, 1)

            noIntent.putExtra(KEY_EVENT_ID, eventID)
            noIntent.putExtra(KEY_COURSE_ID, courseID)
            noIntent.putExtra(KEY_ACTION, 2)

            cancelIntent.putExtra(KEY_EVENT_ID, eventID)
            cancelIntent.putExtra(KEY_COURSE_ID, courseID)
            cancelIntent.putExtra(KEY_ACTION, 3)

            val broadcastYesPI = PendingIntent.getBroadcast(context, REQ_NOTI_YES_PRESSED, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val broadcastNoPI = PendingIntent.getBroadcast(context, REQ_NOTI_NO_PRESSED, noIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val broadcastCancelPI = PendingIntent.getBroadcast(context, REQ_NOTI_CANCELL_PRESSED, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val notification = NotificationCompat.Builder(context!!,NOTIFICATION_CHANNEL_CLASS_ID)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_noti_icon)
                .setContentTitle(eventName)
                .setContentIntent(activityPIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentText("Did you attend $eventName")
                .addAction(R.drawable.ic_noti_icon,"YES",broadcastYesPI)
                .addAction(R.drawable.ic_noti_icon,"NO",broadcastNoPI)
                .addAction(R.drawable.ic_noti_icon,"CANCEL",broadcastCancelPI)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notify((Calendar.getInstance().timeInMillis%1000).toInt(),notification)
        }else{
            val notification = Notification.Builder(context, NOTIFICATION_CHANNEL_EVENT_ID)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_noti_icon)
                .setContentTitle(eventName)
                .setContentIntent(activityPIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentText("You have an upcoming event $eventName")
                .build()

            val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notify(NOTIFICATION_NOTIFICATION_EVENT_ID,notification)
        }

    }

}