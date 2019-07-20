package com.example.nittcompanion.model.repository.implementations

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.nittcompanion.common.*
import com.example.nittcompanion.common.objects.Alert
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import com.example.nittcompanion.model.repository.IRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.*


class RepoImplementation private constructor() : IRepo {

    override suspend fun initialise() {
        courses.value = listOf()
        alerts.value = listOf()
        loadCourses()
        loadAlerts()
        listenCourses()
        listenAlerts()
    }

    companion object {
        @Volatile
        private var instance: IRepo? = null

        fun getInstance(): IRepo = instance ?: synchronized(this) {
            instance ?: RepoImplementation().also { instance = it }
        }
    }

    private val user = FirebaseAuth.getInstance().currentUser
    private val alertReference =
        FirebaseFirestore.getInstance().collection("user").document(user!!.uid).collection(FIREBASE_COLLECTION_ALERTS)
    private val eventsReference =
        FirebaseFirestore.getInstance().collection("user").document(user!!.uid).collection(FIREBASE_COLLECTION_EVENTS)
    private val courseReference =
        FirebaseFirestore.getInstance().collection("user").document(user!!.uid).collection(FIREBASE_COLLECTION_COURSES)

    private val alerts = MutableLiveData<List<Alert>>()
    private val courses = MutableLiveData<List<Course>>()

    private suspend fun loadCourses() {
        try {
            val result = awaitTaskResult(courseReference.get())
            when (val res = resultToCoursesList(result)) {
                is Result.Value -> courses.value = res.value
                is Result.Error -> logEror("loading courses", res.error)
            }
        } catch (e: Exception) {
            logEror("loading courses", e)
        }
    }

    private suspend fun loadAlerts() {
        try {
            val result = awaitTaskResult(alertReference.get())
            when (val res = resultToAlertsList(result)) {
                is Result.Value -> alerts.value = res.value
                is Result.Error -> logEror("loading courses", res.error)
            }
        } catch (e: Exception) {
            logEror("loading courses", e)
        }
    }

    private fun listenAlerts() {
        alertReference.addSnapshotListener { querrySnapshot, exception ->
            if (exception != null) {
                logEror("error listening alerts", exception)
                return@addSnapshotListener
            }
            when (val res = resultToAlertsList(querrySnapshot)) {
                is Result.Value -> alerts.value = res.value
                is Result.Error -> logEror("", res.error)
            }
        }
    }

    private fun listenCourses() {
        courseReference.addSnapshotListener { querrySnapshot, exception ->
            if (exception != null) {
                logEror("error listening courses", exception)
                return@addSnapshotListener
            }
            when (val res = resultToCoursesList(querrySnapshot)) {
                is Result.Value -> courses.value = res.value
                is Result.Error -> logEror("", res.error)
            }
        }
    }

    override suspend fun getEventsIn(field: Int, date: Calendar): Result<Exception, List<Event>> =
        when (field) {
            Calendar.DAY_OF_MONTH -> getEventsOnDate(date)
            Calendar.MONTH -> getEventsInMonth(date)
            else -> Result.build { throw IllegalArgumentException("Illegal argument passed to get events in") }
        }

    override suspend fun getEventByID(iD: String): Result<Exception, Event>  = try{
        val eventDocSnapshot = awaitTaskResult(eventsReference.document(iD).get())
        var event = Event()
        if(eventDocSnapshot.exists()) {
            event = eventDocSnapshot.toObject(Event::class.java)!!
            event.ID = eventDocSnapshot.id
        }
        Result.build { event }
    } catch (e : Exception){
        Result.build { throw e }
    }

    override suspend fun getCourseById(id : String): Result<Exception, Course> = try {
        val courseDocSnap = awaitTaskResult(courseReference.document(id).get())
        var course = Course()
        if(courseDocSnap.exists()){
            course = courseDocSnap.toObject(Course::class.java)!!
            course.ID = courseDocSnap.id
        }
        Result.build { course }
    } catch (e : Exception){
        Result.build { throw e }
    }


    override suspend fun getAlertEvents(courseid: String): Result<Exception, List<Event>> =
        try {
            val task1 = awaitTaskResult(eventsReference.whereEqualTo("courceid", courseid)
                .whereEqualTo("type", TYPE_ASSIGNMENT)
                .get()
            )
            val task2 = awaitTaskResult(eventsReference.whereEqualTo("courceid", courseid)
                .whereEqualTo("type", TYPE_CT)
                .get()
            )
            val task3 = awaitTaskResult(eventsReference.whereEqualTo("courceid", courseid)
                .whereEqualTo("type", TYPE_ENDSEM)
                .get()
            )
            resultToEventsList(task1,task2,task3)
        } catch (e: Exception) {
            Result.build { throw e }
        }

    override suspend fun getUpcommingEvents(noOfEvents: Int): Result<Exception, List<Event>> =
        try {
            val task = awaitTaskResult(
                eventsReference.whereGreaterThanOrEqualTo("startDate", Calendar.getInstance().timeInMillis)
                    .limit(noOfEvents.toLong())
                    .get()
            )
            resultToEventsList(task)
        } catch (e: Exception) {
            Result.build { throw e }
        }

    override fun getCources(): LiveData<List<Course>> = courses

    override suspend fun updateCourse(course: Course, SyncInFirebase: Boolean): Result<Exception, Unit> =
        try{
            val updateCourseRef = courseReference.document(course.ID)
            awaitTaskCompletable(updateCourseRef.set(course))
            Result.build { Unit }
        } catch (e : Exception){
            Result.build { throw e }
        }

    override suspend fun updateEvent(event: Event): Result<Exception, Unit> =
        try {
            val updateEventRef = eventsReference.document(event.ID)
            awaitTaskCompletable(updateEventRef.set(event))
            Result.build { Unit }
        } catch (e: Exception) {
            Result.build { throw e }
        }

    override suspend fun removeCourse(course: Course): Result<Exception, Unit> =
        try {
            val deleteCourseRerf = courseReference.document(course.ID)
            awaitTaskCompletable(
                deleteCourseRerf.delete()
            )
            Result.build { Unit }
        } catch (e: Exception) {
            Result.build { throw e }
        }

    override suspend fun removeEvent(event: Event): Result<Exception, Unit> =
        try {
            val deleteEventRerf = eventsReference.document(event.ID)
            val alert = alerts.value!!.find {
                it.eventId == event.ID
            }
            if(alert  != null){
                removeAlert(alert)
            }
            awaitTaskCompletable(
                deleteEventRerf.delete()
            )
            Result.build { Unit }
        } catch (e: Exception) {
            Result.build { throw e }
        }

    override fun getAlerts(): LiveData<List<Alert>> = alerts

    override suspend fun addAlert(alert: Alert): Result<Exception, Unit> =
        try {
            val addAlertRef = alertReference.document(alert.eventId)
            awaitTaskCompletable(
                addAlertRef.set(alert)
            )
            Result.build { Unit }
        } catch (e: Exception) {
            Result.build { throw e }
        }

    override suspend fun removeAlert(alert: Alert): Result<Exception, Unit> =
        try {
            val deleteAlertRerf = alertReference.document(alert.eventId)
            awaitTaskCompletable(
                deleteAlertRerf.delete()
            )
            Result.build { Unit }
        } catch (e: Exception) {
            Result.build { throw e }
        }

    private suspend fun getEventsInMonth(month: Calendar): Result<Exception, List<Event>> =
        try {
            val task = awaitTaskResult(
                eventsReference.whereEqualTo("month", month.getMonthInFormat())
                    .get()
            )
            resultToEventsList(task)
        } catch (e: Exception) {
            Result.build { throw e }
        }

    private suspend fun getEventsOnDate(date: Calendar): Result<Exception, List<Event>> =
        try {
            val task = awaitTaskResult(
                eventsReference.whereEqualTo("date", date.getDateInFormat())
                    .get()
            )
            resultToEventsList(task)
        } catch (e: Exception) {
            Result.build { throw e }
        }


    private fun resultToCoursesList(result: QuerySnapshot?): Result<Exception, List<Course>> {
        val courseList = mutableListOf<Course>()

        result?.forEach { documentSnapshot ->
            val course = documentSnapshot.toObject(Course::class.java)
            course.ID = documentSnapshot.id
            courseList.add(course)
        }

        return Result.build { courseList }
    }

    private fun resultToEventsList(vararg result: QuerySnapshot?): Result<Exception, List<Event>> {
        val eventsList = mutableListOf<Event>()

        result.forEach { qs ->
            qs?.forEach { documentSnapshot ->
                val event = documentSnapshot.toObject(Event::class.java)
                event.ID = documentSnapshot.id
                eventsList.add(event)
            }

        }

        return Result.build {
            eventsList.sortedBy {
                it.startDate
            }
        }
    }

    private fun resultToAlertsList(result: QuerySnapshot?): Result<Exception, List<Alert>> {
        val alertList = mutableListOf<Alert>()

        result?.forEach { documentSnapshot ->
            val alert = documentSnapshot.toObject(Alert::class.java)
            alert.eventId = documentSnapshot.id
            alertList.add(alert)
        }

        return Result.build {
            alertList
        }
    }

    private fun logEror(message: String, e: Exception) {
        Log.e("Repository", message, e)
    }

}