package com.example.nittcompanion.model.repository.implementations

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.nittcompanion.common.*
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import com.example.nittcompanion.model.repository.IRepo
import com.example.nittcompanion.notification.NotifyAlert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.*


class RepoImplementation private constructor(val app :Application) : IRepo {

    override suspend fun initialise() {

        loadCourses()
        listenCourses()
    }

    companion object {
        @Volatile
        private var instance: IRepo? = null

        fun getInstance(app : Application): IRepo = instance ?: synchronized(this) {
            instance ?: RepoImplementation(app).also { instance = it }
        }
    }

    private val user = FirebaseAuth.getInstance().currentUser
    private val fireStoreInstance = FirebaseFirestore.getInstance().collection("user").document(user!!.uid)
    private val eventsReference = fireStoreInstance.collection(FIREBASE_COLLECTION_EVENTS)
    private val courseReference = fireStoreInstance.collection(FIREBASE_COLLECTION_COURSES)

    private val courses = MutableLiveData<List<Course>>()

    private suspend fun loadCourses() = try {
        Log.d("Load cources","started loading courses")
        val result = awaitTaskResult(courseReference.get())
        when (val res = resultToCoursesList(result)) {
            is Result.Value -> courses.value = res.value
            is Result.Error -> logEror("loading courses", res.error)
        }
        courses.value?.forEach {
            if(it.lastEventCreated <= Calendar.getInstance().timeInMillis){
                addEventsForWeek(it)
            }
        }
        Log.d("Load cources","finished loading courses")
    } catch (e: Exception) {
        logEror("loading courses", e)
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
        Log.e("REPO","id opassed is $iD")
        val eventDoc = eventsReference.document(iD)
        val eventDocSnapshot = awaitTaskResult(eventDoc.get())
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

    override suspend fun updateCourse(course: Course): Result<Exception, Unit> =
        try{
            val updateCourseRef = courseReference.document(course.ID)
            awaitTaskCompletable(updateCourseRef.set(course))
            if (course.lastEventCreated == 0.toLong())
                addEventsForWeek(course)
            Log.e("REPO","update course id ${course.ID}")
            Result.build { Unit }
        } catch (e : Exception){
            Log.e("REPO","update course failed in repo")
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
            awaitTaskCompletable(
                deleteEventRerf.delete()
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
            val events = mutableListOf<Event>()
            val cal = Calendar.getInstance()
            cal[Calendar.HOUR_OF_DAY] = 0
            cal[Calendar.MINUTE] = 0
            cal[Calendar.SECOND] = 1
            cal.add(Calendar.DAY_OF_MONTH,(cal[Calendar.DAY_OF_WEEK] - Calendar.SUNDAY)* -1)
            val timeflag = cal.timeInMillis
            courses.value?.filter{
                it.lastEventCreated < timeflag
            }?.forEach {
                it.getRegularClasseOnDay(date)?.let { event ->
                    events.add(event)
                }

            }
            val task = awaitTaskResult(
                eventsReference.whereEqualTo("date", date.getDateInFormat())
                    .get()
            )
            when(val res = resultToEventsList(task)){
                is Result.Value -> events.addAll(res.value)
                is Result.Error -> Log.wtf("loading event failed",res.error)
            }
            Result.build { events.sortedBy { it.startDate } }
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


    private fun logEror(message: String, e: Exception) {
        Log.e("Repository", message, e)
    }

    private suspend fun addEventsForWeek(course : Course){
        val sentDate = Calendar.getInstance()
        var lastEnd : Long = 0
        if(sentDate[Calendar.DAY_OF_WEEK]>=Calendar.FRIDAY)
            sentDate.add(Calendar.DAY_OF_MONTH,4)
        course.getRegularClasseForWeek(sentDate).let { events ->
            events.forEach { event ->
                Log.d("add eventsFor Week","eventname : ${event.name} event id ${event.ID} course id ${event.courceid}")
                val notifyIntent = Intent(app,NotifyAlert::class.java)
                notifyIntent.putExtra(KEY_EVENT_ID, event.ID)
                notifyIntent.putExtra(KEY_EVENT_NAME, event.name)
                notifyIntent.putExtra(KEY_COURSE_ID,event.courceid)
                notifyIntent.putExtra(KEY_IS_CLASS,event.type in arrayOf(TYPE_CLASS, TYPE_LAB))
                if(PendingIntent.getBroadcast(app,event.ID.takeLast(5).toInt(),notifyIntent,
                        PendingIntent.FLAG_NO_CREATE)==null) {
                    val notifyPendingIntent = PendingIntent.getBroadcast(
                        app.applicationContext, event.ID.takeLast(5).toInt(), notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    val alarmManager =
                        app.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    alarmManager.set(AlarmManager.RTC_WAKEUP, event.endDate, notifyPendingIntent)
                }
                awaitTaskCompletable(eventsReference.document(event.ID).set(event))
                lastEnd = event.endDate
            }
        }
        awaitTaskCompletable(courseReference.document(course.ID).update("lastEventCreated",lastEnd))
    }

}