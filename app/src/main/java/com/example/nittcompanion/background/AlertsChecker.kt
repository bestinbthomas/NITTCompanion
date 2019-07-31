package com.example.nittcompanion.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.nittcompanion.common.*
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.notification.NotifyAlert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AlertsChecker(appContext: Context,workerParams : WorkerParameters) : Worker(appContext,workerParams) {
    private val firestoreInstance =  FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser
    private val eventReference  =
        firestoreInstance.collection("user").document(user!!.uid).collection(FIREBASE_COLLECTION_EVENTS)
    private val courseReference =
        firestoreInstance.collection("user").document(user!!.uid).collection(FIREBASE_COLLECTION_COURSES)
    override fun doWork(): Result {
        try{
            Log.d("AlertsChecker","running")
            val cal = Calendar.getInstance()
            cal[Calendar.HOUR_OF_DAY] = 0
            cal[Calendar.MINUTE] = 0
            cal[Calendar.SECOND] = 0
            cal.add(Calendar.DAY_OF_MONTH,(cal[Calendar.DAY_OF_WEEK] - Calendar.SUNDAY) * -1)
            Log.e("AlertsChecker","date to check ${cal.getDateInFormat()} time to check ${cal.getTimeInFormat()}")
            val timeflag = cal.timeInMillis
            courseReference.whereLessThan("lastEventCreated",timeflag)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("AlertsChecker","success on getting courses")
                    val courses = mutableListOf<Course>()
                    querySnapshot.forEach { ds ->
                        val course = ds.toObject(Course::class.java)
                        course.ID = ds.id
                        courses.add(course)
                    }

                    courses.forEach { course ->
                        val sentDate = Calendar.getInstance()
                        var lastend :Long = 0
                        if(sentDate[Calendar.DAY_OF_WEEK]>=Calendar.FRIDAY)
                            sentDate.add(Calendar.DAY_OF_MONTH,4)
                        course.getRegularClasseForWeek(sentDate).let { events ->
                            events.forEach { event ->
                                val notifyIntent = Intent(applicationContext,NotifyAlert::class.java)
                                notifyIntent.putExtra(KEY_EVENT_ID, event.ID)
                                notifyIntent.putExtra(KEY_EVENT_NAME, event.name)
                                notifyIntent.putExtra(KEY_COURSE_ID,event.courceid)
                                notifyIntent.putExtra(KEY_IS_CLASS,true)
                                val notifyPendingIntent = PendingIntent.getBroadcast(applicationContext,event.ID.takeLast(5).toInt(),notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT)
                                val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                alarmManager.set(AlarmManager.RTC_WAKEUP,event.endDate,notifyPendingIntent)

                                eventReference.document(event.ID).set(event)
                                lastend = event.endDate
                            }
                        }
                        Log.e("AlertsChecker","last updated to $lastend")
                        courseReference.document(course.ID).update("lastEventCreated",lastend)
                    }
                }
                .addOnFailureListener {
                    throw it
                }

        }catch (e : Exception){
            Log.e("AlertsChecker","failed!!",e)
            return Result.retry()
        }
        return Result.success()
    }
}