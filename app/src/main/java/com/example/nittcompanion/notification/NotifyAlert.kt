package com.example.nittcompanion.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.nittcompanion.R
import com.example.nittcompanion.common.*
import java.util.*

class NotifyAlert(context: Context,params: WorkerParameters) : Worker(context,params) {
    override fun doWork(): Result {
        val eventID = inputData.getString(KEY_EVENT_ID)
        val eventName = inputData.getString(KEY_EVENT_NAME)
        val courseID = inputData.getString(KEY_COURSE_ID)
        val isClass = inputData.getBoolean(KEY_IS_CLASS,false)
        val intentOnTouch = Intent(applicationContext, MainActivity::class.java)
        intentOnTouch.putExtra(KEY_EVENT_ID,eventID)
        intentOnTouch.putExtra(KEY_COURSE_ID,courseID)
        intentOnTouch.putExtra(KEY_IS_CLASS,isClass)

        val activityPIntent = PendingIntent.getActivity(applicationContext, REQ_NOTI_MAIN_ACTIVITY, intentOnTouch, 0)

        val icon :Icon = Icon.createWithResource(applicationContext,R.drawable.ic_app_launcher_foreground)

        val builder = Notification.Builder(applicationContext,
            if (isClass) NOTIFICATION_CHANNEL_CLASS_ID else NOTIFICATION_CHANNEL_EVENT_ID)
            .setSmallIcon(icon)
            .setContentTitle(eventName)
            .setContentIntent(activityPIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
        if (isClass){
            val yesIntent = Intent(applicationContext, NotificationBroadCastReceiver::class.java)
            val noIntent = Intent(applicationContext, NotificationBroadCastReceiver::class.java)
            val cancellIntent = Intent(applicationContext, NotificationBroadCastReceiver::class.java)
                yesIntent.putExtra(KEY_EVENT_ID, eventID)
                yesIntent.putExtra(KEY_COURSE_ID, courseID)
                yesIntent.putExtra(KEY_ACTION, 1)
                noIntent.putExtra(KEY_EVENT_ID, eventID)
                noIntent.putExtra(KEY_COURSE_ID, courseID)
                noIntent.putExtra(KEY_ACTION, 2)
                cancellIntent.putExtra(KEY_EVENT_ID, eventID)
                cancellIntent.putExtra(KEY_COURSE_ID, courseID)
                cancellIntent.putExtra(KEY_ACTION, 3)
            val broadcastYes = PendingIntent.getBroadcast(applicationContext, REQ_NOTI_YES_PRESSED, yesIntent, 0)
            val broadcastNo = PendingIntent.getBroadcast(applicationContext, REQ_NOTI_NO_PRESSED, noIntent, 0)
            val broadcastCancel = PendingIntent.getBroadcast(applicationContext, REQ_NOTI_CANCELL_PRESSED, cancellIntent, 0)
            builder.setContentText("Did you attend the class")
                .addAction(Notification.Action.Builder(icon,"YES",broadcastYes).build())
                .addAction(Notification.Action.Builder(icon,"NO",broadcastNo).build())
                .addAction(Notification.Action.Builder(icon,"CANCEL",broadcastCancel).build())
        }else{
            builder.setContentText("You have an upcoming event")
        }

        val notification = builder.build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(if(isClass) (Calendar.getInstance().timeInMillis%1000).toInt() else NOTIFICATION_NOTIFICATION_EVENT_ID,notification)

        return Result.success()
    }
}