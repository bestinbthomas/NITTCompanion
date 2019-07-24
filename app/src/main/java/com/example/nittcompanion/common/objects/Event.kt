package com.example.nittcompanion.common.objects


import com.example.nittcompanion.common.getCalEnderWithMillis
import com.example.nittcompanion.common.getDateInFormat
import com.example.nittcompanion.common.getMonthInFormat
import com.google.firebase.firestore.Exclude
import java.util.*

data class Event(var name:String = "",
                 var startDate: Long = Calendar.getInstance().timeInMillis,
                 var endDate: Long = Calendar.getInstance().timeInMillis,
                 var type : String ="",
                 var courceid: String = "",
                 @set:Exclude @get:Exclude var ID : String = startDate.toString()) {
    var month : String = Calendar.getInstance().getCalEnderWithMillis(startDate).getMonthInFormat()
    var date : String = Calendar.getInstance().getCalEnderWithMillis(startDate).getDateInFormat()

}