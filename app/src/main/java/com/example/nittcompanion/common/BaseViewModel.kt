package com.example.nittcompanion.common

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import com.example.nittcompanion.model.repository.IRepo
import com.example.nittcompanion.notification.NotifyAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class BaseViewModel(private val uicontext: CoroutineContext, private val repo:IRepo, private val app : Application) : ViewModel() ,CoroutineScope{

    private val TAG = "BaseViewModel"
    private val jobTracker: Job

    private val curDate = MutableLiveData<Calendar>()
    val DispDate : LiveData<Calendar> get() = curDate

    private val privateMonthlyEvents = MutableLiveData<List<Event>>()
    val MonthlyEvents : LiveData<List<Event>> get() = privateMonthlyEvents

    private val privateSelectableEvents = MutableLiveData<List<Event>>()
    val SelectableEvents : LiveData<List<Event>> get() = privateSelectableEvents

    private val SelEvent = MutableLiveData<Event>()
    val DispEvent : LiveData<Event> get() = SelEvent

    val Courses : LiveData<List<Course>> = Transformations.map(repo.getCources()){
        it
    }

    private val SelCourse = MutableLiveData<Course>()
    val DispCourse: LiveData<Course> get() = SelCourse

    init {
        privateMonthlyEvents.value = listOf()
        privateSelectableEvents.value = listOf()
        SelEvent.value = Event()
        SelCourse.value = Course()
        jobTracker = Job()
        curDate.value = Calendar.getInstance()
    }

    fun listen(listenTo : ListenTo){
        Log.d("TAG","Listen to called")
        when(listenTo){
            is ListenTo.NextMonthClicked -> changeMonth(1)
            is ListenTo.PreviousMonthClicked -> changeMonth(-1)
            is ListenTo.DateSelected -> changeDate(listenTo.date)
            is ListenTo.EventClicked -> selectEvent(listenTo.position)
            is ListenTo.ScheduleFragmentstart -> initScheduleFragment()
            is ListenTo.CourseSelected -> selectCourse(listenTo.position)
            is ListenTo.CancellEvent -> removeEvent()
            is ListenTo.ClassAttended -> classAttended()
            is ListenTo.ClassBunked -> classBunked()
            is ListenTo.AddEventForCourse -> addEventForCourse()
            is ListenTo.RemoveCourse -> removeCourse()
            is ListenTo.AddNewEvent -> addNewEvent()
            is ListenTo.AddNewCourse -> addNewCourse()
            is ListenTo.UpdateCourse -> updateCourse(listenTo.course)
            is ListenTo.UpdateEvent -> updateEvent(listenTo.event)
            is ListenTo.CourseDetailStart -> initCourseDetail()
            is ListenTo.HomeFragmentStart -> initHomeFragment()
            is ListenTo.ActivityStarted -> initialiseRepo()
            is ListenTo.NotificationTappedEvent -> getEventWithId(listenTo.eventID)
            is ListenTo.NotificationTappedCourse -> getCourseWithId(listenTo.courseID)
            is ListenTo.UpdateAttendance -> updateAttendance(listenTo.course)
        }
    }

    private fun initialiseRepo() = launch{
        loadingState.value = true
        repo.initialise()
        loadingState.value = false
    }

    private fun initHomeFragment() = launch{
        privateSelectableEvents.value = listOf()
        loadingState.value = true
        when(val res = repo.getUpcommingEvents(7)){
            is Result.Value -> {
                addNotifications(res.value)
                privateSelectableEvents.value = res.value
            }
            is Result.Error -> {
                errorState.value = ERROR_EVENT_LOAD
                Log.e("ViewModel","error loading events",res.error)
            }
        }
        loadingState.value = false
    }

    private fun addNotifications(events: List<Event>) = launch{
        events.forEach{event ->
            val notifyIntent = Intent(app,NotifyAlert::class.java)
            notifyIntent.putExtra(KEY_EVENT_ID, event.ID)
            notifyIntent.putExtra(KEY_EVENT_NAME, event.name)
            notifyIntent.putExtra(KEY_COURSE_ID,event.courceid)
            notifyIntent.putExtra(KEY_IS_CLASS,event.type in arrayOf(TYPE_CLASS, TYPE_LAB))
            if(PendingIntent.getBroadcast(app,event.ID.takeLast(5).toInt(),notifyIntent,
                    PendingIntent.FLAG_NO_CREATE)==null) {
                val notifyPendingIntent = PendingIntent.getBroadcast(
                    app, event.ID.takeLast(5).toInt(), notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                val alarmManager =
                    app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val time = if(event.type in arrayOf(TYPE_CLASS, TYPE_LAB))event.endDate else event.startDate - (30*60*1000)
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, notifyPendingIntent)
            }
        }
    }

    private fun initCourseDetail() =launch{
        privateSelectableEvents.value = listOf()
        loadingState.value = true
         when(val res = repo.getAlertEvents(DispCourse.value!!.ID)){
            is Result.Value -> privateSelectableEvents.value = res.value
            is Result.Error -> {
                Log.e("ViewModel","error loading events",res.error)
                errorState.value = ERROR_EVENT_LOAD
            }
        }
        loadingState.value = false
    }

    private fun updateEvent(event: Event) {
        if (privateSelectableEvents.value!!.none {
                it.ID == event.ID
            }) {
            val events = SelectableEvents.value!!.filter { true } as MutableList
            events.add(event)
            privateSelectableEvents.value = events
            }
        launch { evaluateResult("update event",repo.updateEvent(event)) }
    }

    private fun updateCourse(course: Course) {
        launch {  evaluateResult("update courses",repo.updateCourse(course))}
    }

    private fun updateAttendance(course: Course) {
        launch {  evaluateResult("update courses",repo.updateAttendance(course))}
    }

    private fun addNewCourse() {
        SelCourse.value = Course()
    }

    private fun addNewEvent() {
        SelEvent.value = Event()
    }

    private fun removeCourse() = launch{
        when(val res = repo.removeCourse(DispCourse.value!!)){
            is Result.Value -> SelCourse.value = Course()
            is Result.Error -> {
                Log.e("ViewModel","error loading events",res.error)
                errorState.value = ERROR_REMOVING_COURSE
            }
        }
    }

    private fun addEventForCourse() {
        SelEvent.value = Event(name = SelCourse.value!!.name,courceid = SelCourse.value!!.ID,type = TYPE_CLASS,imp = true)
    }

    private fun classBunked() =launch{
        val course = SelCourse.value
        course!!.classBunked()
        evaluateResult("update courses in class bunked",repo.updateCourse(course))
        evaluateResult("update event in class bunked",repo.updateEvent(DispEvent.value!!))
        val notifyIntent = Intent(app, NotifyAlert::class.java)
        PendingIntent.getBroadcast(
            app, DispEvent.value!!.ID.takeLast(5).toInt(),notifyIntent,
            PendingIntent.FLAG_NO_CREATE)?.cancel()

    }

    private fun classAttended() =launch{
        val course = SelCourse.value
        course!!.classAttended()
        evaluateResult("update courses",repo.updateCourse(course))
        evaluateResult("update event in class attended",repo.updateEvent(DispEvent.value!!))
        val notifyIntent = Intent(app, NotifyAlert::class.java)
        PendingIntent.getBroadcast(
            app, DispEvent.value!!.ID.takeLast(5).toInt(),notifyIntent,
            PendingIntent.FLAG_NO_CREATE)?.cancel()
    }

    private fun getEventWithId(id : String) = launch {
        loadingState.value = true
        when(val res = repo.getEventByID(id)){
            is Result.Value -> SelEvent.value = res.value
            is Result.Error -> {
                errorState.value = ERROR_EVENT_LOAD
                Log.e("BASE VIEW MODEL","Failed Loading event ",res.error)
            }
        }
        loadingState.value = false
    }

    private fun getCourseWithId(id : String) = launch {
        Courses.value?.find {
            it.ID ==id
        }?.let {
            SelCourse.value = it
            return@launch
        }
        loadingState.value = true
        when(val res = repo.getCourseById(id)){
            is Result.Value -> SelCourse.value = res.value
            is Result.Error -> {
                Log.e("ViewModel","error loading course",res.error)
                errorState.value = ERROR_COURSE_LOAD
            }
        }
        loadingState.value = false
    }

    private fun removeEvent() =launch{
        when(val res = repo.removeEvent(SelEvent.value!!)){
            is Result.Value -> {
                val event = SelEvent.value!!
                SelEvent.value=null
                val notifyIntent = Intent(app,NotifyAlert::class.java)
                notifyIntent.putExtra(KEY_EVENT_ID, event.ID)
                notifyIntent.putExtra(KEY_EVENT_NAME, event.name)
                notifyIntent.putExtra(KEY_COURSE_ID,event.courceid)
                notifyIntent.putExtra(KEY_IS_CLASS,event.type in arrayOf(TYPE_CLASS, TYPE_LAB))
                PendingIntent.getBroadcast(app,event.ID.takeLast(5).toInt(),notifyIntent,
                        PendingIntent.FLAG_NO_CREATE)?.cancel()
            }
            is Result.Error -> {
                Log.e("ViewModel","error removing events",res.error)
                errorState.value = ERROR_EVENT_REMOVE
            }
        }

    }

    private fun selectCourse(position: Int) {
        SelCourse.value = Courses.value!![position]
    }

    private fun initScheduleFragment() {
        curDate.value = Calendar.getInstance()
        privateSelectableEvents.value = listOf()
        getEventsInMonth()
        getEventsOnDate()
    }

    private fun getEventsInMonth() = launch {
        when(val repoResult = repo.getEventsIn(Calendar.MONTH,date = curDate.value!!)){
            is Result.Value -> privateMonthlyEvents.value = repoResult.value
            is Result.Error -> {
                Log.e("ViewModel","error loading events",repoResult.error)
                errorState.value = ERROR_EVENT_LOAD
            }
        }
    }
    private fun getEventsOnDate() = launch {
        when(val repoResult = repo.getEventsIn(Calendar.DAY_OF_MONTH,date = curDate.value!!)){
            is Result.Value -> privateSelectableEvents.value = repoResult.value
            is Result.Error -> {
                Log.e("ViewModel","error loading events",repoResult.error)
                errorState.value = ERROR_EVENT_LOAD
            }
        }
    }

    private fun changeDate(date: Int) {
        Log.d(TAG,"DateChangedINBase")
        val cal:Calendar = curDate.value!!
        cal.set(Calendar.DAY_OF_MONTH,date)
        curDate.value = cal
        getEventsOnDate()
    }

    private fun changeMonth(change: Int) {
        Log.d(TAG,"MonthChangedINBase")
        val cal:Calendar = curDate.value!!
        cal.add(Calendar.MONTH,change)
        curDate.value = cal
        getEventsInMonth()
        getEventsOnDate()

    }

    private fun selectEvent (pos: Int) {
        SelEvent.value = SelectableEvents.value!![pos]
        Log.e("select event","called")
        if (SelEvent.value!!.type in arrayOf(TYPE_CLASS, TYPE_LAB)) {
            SelCourse.value = Courses.value?.find { it.ID == DispEvent.value!!.courceid } ?: Course()
            Log.e("select event","found courses name ${DispCourse.value!!.name}")
            Log.e("select event","event courseid ${DispEvent.value!!.courceid}")
        }
    }


    private val errorState = MutableLiveData<String>()
    val error: LiveData<String> get() = errorState

    private val loadingState = MutableLiveData<Boolean>()
    val loading : LiveData<Boolean> get() = loadingState

    override val coroutineContext: CoroutineContext
        get() = uicontext+jobTracker

    private fun evaluateResult (name : String, result: Result<Exception,Unit>){
        when(result){
            is Result.Value -> Log.d(TAG,"$name successfully executed")
            is Result.Error -> Log.e(TAG,"$name failed",result.error)
        }
    }
}

