package com.example.nittcompanion.model.repository

import androidx.lifecycle.LiveData
import com.example.nittcompanion.common.*
import com.example.nittcompanion.common.objects.Event
import java.lang.Exception
import java.util.*

interface IEventsRepo {
    suspend fun getEventsOnDate(date:Calendar) : Result<Exception,List<Event>>

    suspend fun getEventsInMonth_Date(field:Int = Calendar.MONTH,value: String) : Result<Exception,List<Event>>

    suspend fun getEventsForCourse(coirseid: String) : Result<Exception,List<Event>>

    suspend fun getEventsByType(eventType: String,courseid:String?) : Result<Exception,List<Event>>

    suspend fun getNextXEvents(noOfEvents: Int,startDate: Calendar = Calendar.getInstance()) : Result<Exception,List<Event>>

    suspend fun createEvent(event: Event) : Result<Exception,Unit>

    suspend fun createReccuringEvent(event: Event,reccur:String= RECCUR_WEEK) : Result<Exception,Unit>
}