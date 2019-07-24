package com.example.nittcompanion.background

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.nittcompanion.common.*
import com.example.nittcompanion.common.objects.Alert
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.notification.NotifyAlert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.util.concurrent.TimeUnit

class AlertsChecker(appContext: Context,workerParams : WorkerParameters) : Worker(appContext,workerParams) {
    private val firestoreInctance =  FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser
    private val alertReference =
        firestoreInctance.collection("user").document(user!!.uid).collection(FIREBASE_COLLECTION_ALERTS)
    private val eventReference  =
        firestoreInctance.collection("user").document(user!!.uid).collection(FIREBASE_COLLECTION_EVENTS)
    private val courseReference =
        firestoreInctance.collection("user").document(user!!.uid).collection(FIREBASE_COLLECTION_COURSES)
    override fun doWork(): Result {
        try{
            Log.d("AlertsChecker","running")
            val cal = Calendar.getInstance()
            cal[Calendar.HOUR_OF_DAY] = 0
            cal[Calendar.MINUTE] = 0
            cal[Calendar.SECOND] = 0
            cal.add(Calendar.DAY_OF_MONTH,(cal[Calendar.DAY_OF_WEEK] - Calendar.SUNDAY)* -1)
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
                        if(sentDate[Calendar.DAY_OF_WEEK]>=Calendar.FRIDAY)
                            sentDate.add(Calendar.DAY_OF_MONTH,4)
                        course.getRegularClasseForWeek(sentDate).let { events ->
                            events.forEach { event ->
                                val data = Data.Builder()
                                data.putString(KEY_EVENT_ID, event.ID)
                                data.putString(KEY_EVENT_NAME, event.name)
                                data.putString(KEY_COURSE_ID,event.courceid)
                                data.putBoolean(KEY_IS_CLASS,true)
                                val delay: Long =
                                    if (calculateDelay(event.startDate) > 0) calculateDelay(event.startDate) else 0
                                val workRequest = OneTimeWorkRequestBuilder<NotifyAlert>()
                                    .setInputData(data.build())
                                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                                    .build()
                                WorkManager.getInstance(applicationContext)
                                    .enqueue(workRequest)
                                val alert = Alert(event.startDate,event.ID)
                                courseReference.document(course.ID).update("lastEventCreated",sentDate.timeInMillis)
                                alertReference.document(alert.eventId).set(alert)
                                eventReference.document(event.ID).set(event)
                            }

                        }
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