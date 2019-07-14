package com.example.nittcompanion.common.objects

import com.example.nittcompanion.common.getDateInFormat
import com.example.nittcompanion.common.getMonthInFormat
import java.util.*

data class Event(val name:String,var startDate: Calendar,var endDate: Calendar,val type : String,val courceid: String = "",val alert: Boolean = false,val imp : Boolean){
    val month : String get() = startDate.getMonthInFormat()
    val date : String get() = startDate.getDateInFormat()
}