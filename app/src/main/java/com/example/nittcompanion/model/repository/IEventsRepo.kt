package com.example.nittcompanion.model.repository

import com.example.nittcompanion.common.RECCUR_WEEK
import com.example.nittcompanion.common.Result
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import java.util.*

interface IEventsRepo {
    suspend fun getEventsOnDate(date:Calendar) : Result<Exception,List<Event>>

    suspend fun getEventsInMonth_Date(field:Int = Calendar.MONTH,value: String) : Result<Exception,List<Event>>

    suspend fun getEventsForCourse(coirseid: String) : Result<Exception,List<Event>>

    suspend fun getEventsByType(eventType: String,courseid:String?) : Result<Exception,List<Event>>

    suspend fun getNextXEvents(noOfEvents: Int,startDate: Calendar = Calendar.getInstance()) : Result<Exception,List<Event>>

    suspend fun createEvent(event: Event) : Result<Exception,Unit>

    suspend fun createReccuringEvent(event: Event,reccur:String= RECCUR_WEEK) : Result<Exception,Unit>

    suspend fun getCources() : Result<Exception,List<Course>>

    suspend fun getCourcesWithID(courseid: String) : Result<Exception,Course>

    suspend fun updateCourse(course: Course) : Result<Exception,Unit>

    suspend fun updateEvent(event: Event) : Result<Exception,Unit>

    suspend fun removeCourse(course: Course) : Result<Exception,Unit>

    suspend fun removeEvent(event: Event) : Result<Exception,Unit>
}