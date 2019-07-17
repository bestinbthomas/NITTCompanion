package com.example.nittcompanion.model.repository.implementations

import com.example.nittcompanion.common.Result
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import com.example.nittcompanion.model.repository.IEventsRepo
import java.util.*

class EventRepoImplementation: IEventsRepo {
    override suspend fun getCourcesWithID(courseid: String): Result<Exception, Course> {
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

    override suspend fun getCources(): Result<Exception, List<Course>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getEventsInMonth_Date(field: Int, value: String): Result<Exception, List<Event>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getEventsOnDate(date: Calendar): Result<Exception, List<Event>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getEventsForCourse(coirseid: String): Result<Exception, List<Event>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getEventsByType(eventType: String, courseid: String?): Result<Exception, List<Event>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getNextXEvents(noOfEvents: Int, startDate: Calendar): Result<Exception, List<Event>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun createEvent(event: Event): Result<Exception, Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun createReccuringEvent(event: Event, reccur: String): Result<Exception, Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}