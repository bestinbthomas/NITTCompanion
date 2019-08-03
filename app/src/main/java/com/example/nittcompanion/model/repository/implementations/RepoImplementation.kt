package com.example.nittcompanion.model.repository.implementations

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.nittcompanion.common.*
import com.example.nittcompanion.common.objects.Attendence
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import com.example.nittcompanion.common.objects.FireCourse
import com.example.nittcompanion.model.repository.IRepo
import com.example.nittcompanion.notification.NotifyAlert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.io.FileNotFoundException
import java.util.*


class RepoImplementation private constructor(val app: Application) : IRepo {
    private val user = FirebaseAuth.getInstance().currentUser
    private val fireStoreUserInstance = FirebaseFirestore.getInstance().collection("user").document(user!!.uid)
    private val fireStoreClassInstance = FirebaseFirestore.getInstance().collection("class")
    private val userEventsReference = fireStoreUserInstance.collection(FIREBASE_COLLECTION_EVENTS)
    private val userAttendanceReference = fireStoreUserInstance.collection(FIREBASE_COLLECTION_ATTENDANCE)
    private val classEventsReference: CollectionReference
    private val coursesReference: CollectionReference

    private val courses = MutableLiveData<List<Course>>()

    init {
        val mClass = app.getSharedPreferences(SHARED_PREF, MODE_PRIVATE).getString(KEY_CLASS, "noClass")
        coursesReference = fireStoreClassInstance.document(mClass!!).collection(FIREBASE_COLLECTION_COURSES)
        classEventsReference = fireStoreClassInstance.document(mClass).collection(FIREBASE_COLLECTION_EVENTS)
    }

    override suspend fun initialise() {

        loadCourses()
    }

    companion object {
        @Volatile
        private var instance: IRepo? = null

        fun getInstance(app: Application): IRepo = instance ?: synchronized(this) {
            instance ?: RepoImplementation(app).also { instance = it }
        }
    }


    private suspend fun loadCourses() = try {
        Log.d("Load cources", "started loading courses")
        val resultCourse = awaitTaskResult(coursesReference.get())
        val resultAttendence = awaitTaskResult(userAttendanceReference.get())
        when (val res = resultToCoursesList(resultCourse, resultAttendence)) {
            is Result.Value -> courses.value = res.value
            is Result.Error -> logEror("loading courses", res.error)
        }
        courses.value?.forEach {
            if (it.lastEventCreated <= Calendar.getInstance().timeInMillis) {
                addEventsForWeek(it)
            }
        }
        listenCourses()
        listenAttendance()
        Log.d("Load cources", "finished loading courses")
    } catch (e: Exception) {
        logEror("loading courses", e)
    }

     private fun listenCourses()  {
         coursesReference.addSnapshotListener { querrySnapshot, exception ->
             if (exception != null) {
                 logEror("error listening courses", exception)
                 return@addSnapshotListener
             }
              for(docChange in querrySnapshot!!.documentChanges){
                  courses.value = when(docChange.type){
                      DocumentChange.Type.ADDED -> courses.value//courseAdded(docChange.document)
                      DocumentChange.Type.MODIFIED -> courseModified(docChange.document)
                      DocumentChange.Type.REMOVED -> courseRemoved(docChange.document)
                  }
             }
         }
     }

    private fun courseRemoved(document: QueryDocumentSnapshot) : List<Course>{
        val refcourses = getCources().value!! as MutableList
        val course = document.toObject(FireCourse::class.java)
        course.ID = document.id
        val index = refcourses.indexOfFirst{ it.ID == course.ID }
        userAttendanceReference.document(course.ID).delete()
        refcourses.removeAt(index)
        return  refcourses
    }

    private fun courseModified(document: QueryDocumentSnapshot) : List<Course>{
        val refcourses = getCources().value!! as MutableList
        val course = document.toObject(FireCourse::class.java)
        val index = refcourses.indexOfFirst{ it.ID == course.ID }
        if(index!=-1)
            refcourses[index] = Course(course.name,course.credit,course.classEvent,course.ID,course.lastEventCreated)
        return  refcourses
    }

    /*private fun courseAdded(document: QueryDocumentSnapshot) : List<Course>{
        val refcourses = getCources().value!! as MutableList
        val course = document.toObject(FireCourse::class.java)
        if(refcourses.none { it.ID == course.ID }) {
            refcourses.add(Course(course.name, course.credit, course.classEvent, course.ID, course.lastEventCreated))
            userAttendanceReference.document(course.ID).set(Attendence())
        }
        return  refcourses
    }*/

    private fun listenAttendance()  {
         userAttendanceReference.addSnapshotListener { querrySnapshot, exception ->
             if (exception != null) {
                 logEror("error listening courses", exception)
                 return@addSnapshotListener
             }
             val refcourses = getCources().value!!
             for(docChange in querrySnapshot!!.documentChanges) {
                 if(docChange.type == DocumentChange.Type.MODIFIED){
                     val attendence = docChange.document.toObject(Attendence::class.java)
                     attendence.ID = docChange.document.id
                     val index = refcourses.indexOfFirst{ it.ID == attendence.ID }
                     refcourses[index].attended = attendence.attended
                     refcourses[index].notAttended = attendence.notAttended
                 }
             }
             courses.value = refcourses

         }
     }

    override suspend fun getEventsIn(field: Int, date: Calendar): Result<Exception, List<Event>> =
        when (field) {
            Calendar.DAY_OF_MONTH -> getEventsOnDate(date)
            Calendar.MONTH -> getEventsInMonth(date)
            else -> Result.build { throw IllegalArgumentException("Illegal argument passed to get events in") }
        }

    override suspend fun getEventByID(iD: String): Result<Exception, Event> = try {
        Log.e("REPO", "id opassed is $iD")
        var eventDoc = classEventsReference.document(iD)
        val eventDocSnapshot = awaitTaskResult(eventDoc.get())
        val event: Event
        if (eventDocSnapshot.exists()) {
            event = eventDocSnapshot.toObject(Event::class.java)!!
            event.ID = eventDocSnapshot.id
        } else {
            eventDoc = userEventsReference.document(iD)
            val userEventDocSnapshot = awaitTaskResult(eventDoc.get())
            if (userEventDocSnapshot.exists()) {
                event = userEventDocSnapshot.toObject(Event::class.java)!!
                event.ID = userEventDocSnapshot.id
            } else throw FileNotFoundException()
        }
        Result.build { event }
    } catch (e: Exception) {
        Result.build { throw e }
    }

    override suspend fun getCourseById(id: String): Result<Exception, Course> = try {
        val courseDocSnap = awaitTaskResult(coursesReference.document(id).get())
        val attendenceDocSnap = awaitTaskResult(userAttendanceReference.document(id).get())
        var fireCourse = FireCourse()
        if (courseDocSnap.exists()) {
            fireCourse = courseDocSnap.toObject(FireCourse::class.java)!!
            fireCourse.ID = courseDocSnap.id
        }
        var attendence = Attendence()
        if (attendenceDocSnap.exists()) {
            attendence = attendenceDocSnap.toObject(Attendence::class.java)!!
        }
        Result.build { fireCoursetoCourse(fireCourse, attendence) }
    } catch (e: Exception) {
        Result.build { throw e }
    }


    override suspend fun getAlertEvents(courseid: String): Result<Exception, List<Event>> =
        try {
            val task1 = awaitTaskResult(
                classEventsReference.whereEqualTo("courceid", courseid)
                    .whereEqualTo("type", TYPE_ASSIGNMENT)
                    .get()
            )
            val task2 = awaitTaskResult(
                classEventsReference.whereEqualTo("courceid", courseid)
                    .whereEqualTo("type", TYPE_CT)
                    .get()
            )
            val task3 = awaitTaskResult(
                classEventsReference.whereEqualTo("courceid", courseid)
                    .whereEqualTo("type", TYPE_ENDSEM)
                    .get()
            )
            resultToEventsList(task1, task2, task3)
        } catch (e: Exception) {
            Result.build { throw e }
        }

    override suspend fun getUpcommingEvents(noOfEvents: Int): Result<Exception, List<Event>> =
        try {
            val task1 = awaitTaskResult(
                userEventsReference.whereGreaterThanOrEqualTo("startDate", Calendar.getInstance().timeInMillis)
                    .limit(noOfEvents.toLong())
                    .get()
            )

            val task2 = awaitTaskResult(
                classEventsReference.whereGreaterThanOrEqualTo("startDate", Calendar.getInstance().timeInMillis)
                    .limit(noOfEvents.toLong())
                    .get()
            )
            resultToEventsList(task1, task2)
        } catch (e: Exception) {
            Result.build { throw e }
        }

    override fun getCources(): LiveData<List<Course>> = courses

    override suspend fun updateCourse(course: Course): Result<Exception, Unit> =
        try {
            val updateCourseRef = coursesReference.document(course.ID)
            awaitTaskCompletable(updateCourseRef.set(course))
            if (course.lastEventCreated == 0.toLong())
                addEventsForWeek(course)
            Log.e("REPO", "update course id ${course.ID}")
            Result.build { Unit }
        } catch (e: Exception) {
            Log.e("REPO", "update course failed in repo")
            Result.build { throw e }
        }

    override suspend fun updateEvent(event: Event): Result<Exception, Unit> =
        try {

            val updateEventRef =
                if (event.type == TYPE_OTHER)
                    userEventsReference.document(event.ID)
                else
                    classEventsReference.document(event.ID)
            awaitTaskCompletable(updateEventRef.set(event))
            Result.build { Unit }
        } catch (e: Exception) {
            Result.build { throw e }
        }

    override suspend fun removeCourse(course: Course): Result<Exception, Unit> =
        try {
            val deleteCourseRef = coursesReference.document(course.ID)
            awaitTaskCompletable(
                deleteCourseRef.delete()
            )
            Result.build { Unit }
        } catch (e: Exception) {
            Result.build { throw e }
        }
    override suspend fun removeEvent(event: Event): Result<Exception, Unit> =
        try {
            val deleteEventRerf =
                if (event.type == TYPE_OTHER)
                    userEventsReference.document(event.ID)
                else
                    classEventsReference.document(event.ID)
            awaitTaskCompletable(
                deleteEventRerf.delete()
            )
            Result.build { Unit }
        } catch (e: Exception) {
            Result.build { throw e }
        }

    override suspend fun updateAttendance(course: Course): Result<Exception, Unit> =
        try {
            val attendence = Attendence(course.attended, course.notAttended, course.ID)
            awaitTaskCompletable(userAttendanceReference.document(course.ID).set(attendence))
            Result.build { Unit }
        } catch (e: Exception) {
            Result.build { throw e }
        }

    private suspend fun getEventsInMonth(month: Calendar): Result<Exception, List<Event>> =
        try {
            val task1 = awaitTaskResult(
                classEventsReference.whereEqualTo("month", month.getMonthInFormat())
                    .get()
            )
            val task2 = awaitTaskResult(
                userEventsReference.whereEqualTo("month", month.getMonthInFormat())
                    .get()
            )
            resultToEventsList(task1, task2)
        } catch (e: Exception) {
            Result.build { throw e }
        }

    private suspend fun getEventsOnDate(date: Calendar): Result<Exception, List<Event>> =
        try {
            val events = mutableListOf<Event>()
            date[Calendar.HOUR_OF_DAY] = 3
            courses.value?.filter {
                it.lastEventCreated < date.timeInMillis
            }?.forEach {
                it.getRegularClasseOnDay(date)?.let { event ->
                    events.add(event)
                }

            }
            val task1 = awaitTaskResult(
                classEventsReference.whereEqualTo("date", date.getDateInFormat())
                    .get()
            )
            val task2 = awaitTaskResult(
                userEventsReference.whereEqualTo("date", date.getDateInFormat()).get()
            )
            when (val res = resultToEventsList(task1, task2)) {
                is Result.Value -> events.addAll(res.value)
                is Result.Error -> Log.wtf("loading event failed", res.error)
            }
            Result.build { events.sortedBy { it.startDate } }
        } catch (e: Exception) {
            Result.build { throw e }
        }


    private fun resultToCoursesList(
        resultFireCourse: QuerySnapshot,
        resultAttendence: QuerySnapshot
    ): Result<Exception, List<Course>> {
        val courseList = mutableListOf<Course>()
        val fireCourseList = mutableListOf<FireCourse>()
        val attendanceList = mutableListOf<Attendence>()

        resultFireCourse.forEach { documentSnapshot ->
            val fireCourse = documentSnapshot.toObject(FireCourse::class.java)
            fireCourse.ID = documentSnapshot.id
            fireCourseList.add(fireCourse)
        }

        resultAttendence.forEach { documentSnapshot ->
            val attendence = documentSnapshot.toObject(Attendence::class.java)
            attendence.ID = documentSnapshot.id
            attendanceList.add(attendence)
        }

        fireCourseList.forEach { fireCourse ->
            val attendance : Attendence? = attendanceList.find {it.ID == fireCourse.ID }
            when(attendance == null)
            {
                true -> addAttendanceForCourse(fireCourse)
                false -> courseList.add(fireCoursetoCourse(fireCourse,attendance))
            }
        }

        return Result.build { courseList }
    }

    private fun addAttendanceForCourse(fireCourse: FireCourse) {
        userAttendanceReference.document(fireCourse.ID).set(Attendence())
            .addOnSuccessListener {
                Log.d("REPO","attendance added successfully")
            }
            .addOnFailureListener {
                Log.d("REPO","attendance adding failed")
            }
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

    private suspend fun addEventsForWeek(course: Course) {
        val sentDate = Calendar.getInstance()
        var lastEnd: Long = 0
        if (sentDate[Calendar.DAY_OF_WEEK] >= Calendar.FRIDAY)
            sentDate.add(Calendar.DAY_OF_MONTH, 4)
        course.getRegularClasseForWeek(sentDate).let { events ->
            events.forEach { event ->
                Log.d(
                    "add eventsFor Week",
                    "eventname : ${event.name} event id ${event.ID} course id ${event.courceid}"
                )
                val notifyIntent = Intent(app, NotifyAlert::class.java)
                notifyIntent.putExtra(KEY_EVENT_ID, event.ID)
                notifyIntent.putExtra(KEY_EVENT_NAME, event.name)
                notifyIntent.putExtra(KEY_COURSE_ID, event.courceid)
                notifyIntent.putExtra(KEY_IS_CLASS, event.type in arrayOf(TYPE_CLASS, TYPE_LAB))
                if (PendingIntent.getBroadcast(
                        app, event.ID.takeLast(5).toInt(), notifyIntent,
                        PendingIntent.FLAG_NO_CREATE
                    ) == null
                ) {
                    val notifyPendingIntent = PendingIntent.getBroadcast(
                        app.applicationContext, event.ID.takeLast(5).toInt(), notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    val alarmManager =
                        app.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    alarmManager.set(AlarmManager.RTC_WAKEUP, event.endDate, notifyPendingIntent)
                }
                awaitTaskCompletable(classEventsReference.document(event.ID).set(event))
                lastEnd = event.endDate
            }
        }
        awaitTaskCompletable(coursesReference.document(course.ID).update("lastEventCreated", lastEnd))
    }

}