package com.example.nittcompanion.common.objects

import com.google.firebase.firestore.Exclude
import java.util.*

data class Attendence(var attended : Int = 0, var notAttended : Int = 0, @set:Exclude @get:Exclude var ID : String = Calendar.getInstance().timeInMillis.toString())