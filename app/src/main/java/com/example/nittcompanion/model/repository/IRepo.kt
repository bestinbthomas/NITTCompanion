package com.example.nittcompanion.model.repository

import androidx.lifecycle.LiveData
import com.example.nittcompanion.common.Result
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import java.util.*

interface IRepo {

    suspend fun getEventByID(iD :String) : Result<Exception,Event>

    suspend fun getEventsIn(field:Int, date: Calendar) : Result<Exception,List<Event>>

    suspend fun getAlertEvents(courseid:String) : Result<Exception,List<Event>>

    suspend fun getUpcommingEvents(noOfEvents: Int) : Result<Exception,List<Event>>

    fun getCources() : LiveData<List<Course>>

    suspend fun updateCourse(course: Course) : Result<Exception,Unit>

    suspend fun updateAttendance(course: Course) : Result<Exception,Unit>

    suspend fun updateEvent(event: Event) : Result<Exception,Unit>

    suspend fun removeCourse(course: Course) : Result<Exception,Unit>

    suspend fun removeEvent(event: Event) : Result<Exception,Unit>

    suspend fun initialise()

    suspend fun getCourseById(id : String): Result<Exception,Course>

}