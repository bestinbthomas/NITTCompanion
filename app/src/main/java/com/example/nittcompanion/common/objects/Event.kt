package com.example.nittcompanion.common.objects


import com.example.nittcompanion.common.getDateInFormat
import com.example.nittcompanion.common.getMonthInFormat
import com.google.firebase.firestore.Exclude
import java.util.*

data class Event(var name:String = "",
                 var startDate: Calendar = Calendar.getInstance(),
                 var endDate: Calendar = Calendar.getInstance(),
                 var type : String ="",
                 var courceid: String = "",
                 @set:Exclude @get:Exclude var ID : String = startDate.timeInMillis.toString()) {
    val month : String get() = startDate.getMonthInFormat()
    val date : String get() = startDate.getDateInFormat()

}