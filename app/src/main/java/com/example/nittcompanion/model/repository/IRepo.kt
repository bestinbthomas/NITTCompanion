package com.example.nittcompanion.model.repository

import androidx.lifecycle.LiveData
import com.example.nittcompanion.common.Result
import com.example.nittcompanion.common.objects.Alert
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import java.util.*

interface IRepo {
    suspend fun getEventsIn(field:Int = Calendar.MONTH, date: Calendar) : Result<Exception,List<Event>>

    suspend fun getEventsByType(courseid:String,vararg eventType: String) : Result<Exception,List<Event>>

    suspend fun getUpcommingEvents(noOfEvents: Int) : Result<Exception,List<Event>>

    fun getCources() : LiveData<List<Course>>

    suspend fun updateCourse(course: Course) : Result<Exception,Unit>

    suspend fun updateEvent(event: Event) : Result<Exception,Unit>

    suspend fun removeCourse(course: Course) : Result<Exception,Unit>

    suspend fun removeEvent(event: Event) : Result<Exception,Unit>

    fun getAlerts() : LiveData<List<Alert>>

    suspend fun addAlert(alert: Alert) : Result<Exception,Unit>

    suspend fun removeAlert(alert: Alert) : Result<Exception,Unit>
}