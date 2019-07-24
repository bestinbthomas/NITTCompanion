package com.example.nittcompanion.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nittcompanion.common.*
import com.example.nittcompanion.common.objects.Course
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationBroadCastReceiver : BroadcastReceiver() {
    private val firestoreInctance =  FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser
    private val alertReference =
        firestoreInctance.collection("user").document(user!!.uid).collection(FIREBASE_COLLECTION_ALERTS)
    private val eventReference  =
        firestoreInctance.collection("user").document(user!!.uid).collection(FIREBASE_COLLECTION_EVENTS)
    private val courseReference =
        firestoreInctance.collection("user").document(user!!.uid).collection(FIREBASE_COLLECTION_COURSES)
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let { mIntent ->
            if (mIntent.hasExtra(KEY_ACTION) and mIntent.hasExtra(KEY_COURSE_ID) and mIntent.hasExtra(KEY_EVENT_ID)){
                val action = mIntent.getIntExtra(KEY_ACTION,0)
                val courseID = mIntent.getStringExtra(KEY_COURSE_ID)
                val eventID = mIntent.getStringExtra(KEY_EVENT_ID)

                when(action){
                    0 -> {}
                    1 -> {  // attended
                        courseReference.document(courseID).get()
                            .addOnSuccessListener {
                                val course = it.toObject(Course::class.java)!!
                                course.ID = it.id
                                course.classAttended()
                                courseReference.document(course.ID).set(course)
                            }
                        alertReference.document(eventID).delete()
                    }
                    2 -> { //bunked
                        courseReference.document(courseID).get()
                            .addOnSuccessListener {
                                val course = it.toObject(Course::class.java)!!
                                course.ID = it.id
                                course.classBunked()
                                courseReference.document(course.ID).set(course)
                            }
                        alertReference.document(eventID).delete()
                    }
                    3 -> {  //cancelled
                        eventReference.document(eventID).delete()
                        alertReference.document(eventID).delete()
                    }
                }
            }
        }
    }
}