package com.example.nittcompanion.model.repository.implementations

import androidx.lifecycle.LiveData
import com.example.nittcompanion.common.Result
import com.example.nittcompanion.common.objects.Alert
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import com.example.nittcompanion.model.repository.IRepo
import java.util.*


class RepoImplementation private constructor(): IRepo {

    companion object {
        @Volatile private var instance :IRepo? = null
        fun getInstance() : IRepo = instance?: synchronized(this) {
            instance ?: RepoImplementation().also { instance = it }
        }
    }

    override suspend fun getEventsIn(field: Int, date: Calendar): Result<Exception, List<Event>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getEventsByType(courseid: String, vararg eventType: String): Result<Exception, List<Event>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getUpcommingEvents(noOfEvents: Int): Result<Exception, List<Event>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCources(): LiveData<List<Course>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun updateCourse(course: Course): Result<Exception, Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun updateEvent(event: Event): Result<Exception, Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun removeCourse(course: Course): Result<Exception, Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun removeEvent(event: Event): Result<Exception, Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAlerts(): LiveData<List<Alert>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun addAlert(alert: Alert): Result<Exception, Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun removeAlert(alert: Alert): Result<Exception, Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}