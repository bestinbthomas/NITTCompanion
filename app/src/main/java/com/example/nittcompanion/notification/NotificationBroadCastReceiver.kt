package com.example.nittcompanion.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nittcompanion.common.*
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

            }
        }
    }
}