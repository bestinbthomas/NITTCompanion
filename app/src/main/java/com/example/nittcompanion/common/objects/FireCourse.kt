package com.example.nittcompanion.common.objects

import com.google.firebase.firestore.Exclude
import java.util.*

class FireCourse(var name : String = "",
                 var credit : Int = 0,
                 var classEvent : ClassEvent = ClassEvent(islab = false),
                 @set:Exclude @get:Exclude var ID : String = Calendar.getInstance().timeInMillis.toString(),
                 var lastEventCreated : Long = 0)