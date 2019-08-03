package com.example.nittcompanion.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.nittcompanion.common.FIREBASE_COLLECTION_ATTENDANCE
import com.example.nittcompanion.common.KEY_ACTION
import com.example.nittcompanion.common.KEY_COURSE_ID
import com.example.nittcompanion.common.KEY_EVENT_ID
import com.example.nittcompanion.common.objects.Attendence
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationBroadCastReceiver : BroadcastReceiver() {
    private val firestoreInctance =  FirebaseFirestore.getInstance()

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let { mIntent ->
            val attendanceReference =
                firestoreInctance.collection("user").document(FirebaseAuth.getInstance().currentUser!!.uid).collection(FIREBASE_COLLECTION_ATTENDANCE)
            if (mIntent.hasExtra(KEY_ACTION) and mIntent.hasExtra(KEY_COURSE_ID) and mIntent.hasExtra(KEY_EVENT_ID)){
                val action = mIntent.getIntExtra(KEY_ACTION,0)
                val courseID = mIntent.getStringExtra(KEY_COURSE_ID)

                Log.e("Receiver","notification action pressed $action")

                when(action){
                    0 -> {}
                    1 -> {  // attended
                        attendanceReference.document(courseID).get()
                            .addOnSuccessListener {
                                val attendence = it.toObject(Attendence::class.java)!!
                                attendence.ID = it.id
                                attendence.attended ++
                                attendanceReference.document(attendence.ID).set(attendence)
                            }
                    }
                    2 -> { //bunked
                        attendanceReference.document(courseID).get()
                            .addOnSuccessListener {
                                val attendence = it.toObject(Attendence::class.java)!!
                                attendence.ID = it.id
                                attendence.notAttended ++
                                attendanceReference.document(attendence.ID).set(attendence)
                            }
                    }
                }
            }
        }
    }
}