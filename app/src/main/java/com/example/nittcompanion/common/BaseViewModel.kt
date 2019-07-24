package com.example.nittcompanion.common

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.nittcompanion.common.objects.Alert
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import com.example.nittcompanion.model.repository.IRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class BaseViewModel(protected val uicontext: CoroutineContext,private val repo:IRepo ) : ViewModel() ,CoroutineScope{

    val alerts: LiveData<List<Alert>> = Transformations.map(repo.getAlerts()){
        it
    }

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
            is ListenTo.AddAlert -> addAlert(listenTo.alert)
            is ListenTo.UpdateCourse -> updateCourse(listenTo.course,listenTo.syncInFireStore)
            is ListenTo.UpdateEvent -> updateEvent(listenTo.event)
            is ListenTo.CourseDetailStart -> initCourseDetail()
            is ListenTo.HomeFragmentStart -> initHomeFragment()
            is ListenTo.ActivityStarted -> initialiseRepo()
            is ListenTo.NotificationTappedEvent -> getEventWithId(listenTo.eventID)
            is ListenTo.NotificationTappedCourse -> getCourseWithId(listenTo.courseID)
        }
    }

    private fun initialiseRepo() = launch{
        repo.initialise()
    }

    private fun initHomeFragment() = launch{
        privateSelectableEvents.value = listOf()
        when(val res = repo.getUpcommingEvents(7)){
            is Result.Value -> privateSelectableEvents.value = res.value
            is Result.Error -> errorState.value = ERROR_EVENT_LOAD
        }
    }

    private fun initCourseDetail() =launch{
        privateSelectableEvents.value = listOf()
         when(val res = repo.getAlertEvents(DispCourse.value!!.ID)){
            is Result.Value -> privateSelectableEvents.value = res.value
            is Result.Error -> errorState.value = ERROR_EVENT_LOAD
        }
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

    private fun updateCourse(course: Course,syncInFirebase : Boolean) {
        launch {  evaluateResult("update courses",repo.updateCourse(course,syncInFirebase))}
    }

    private fun addAlert(alert: Alert) {
       launch { evaluateResult("update courses",repo.addAlert(alert)) }

    }

    private fun addNewCourse() {
        SelCourse.value = Course()
    }

    private fun addNewEvent() {
        SelEvent.value = Event()
    }

    private fun removeCourse() = launch{
        when(repo.removeCourse(DispCourse.value!!)){
            is Result.Value -> SelCourse.value = Course()
            is Result.Error -> errorState.value = ERROR_REMOVING_COURSE
        }
    }

    private fun addEventForCourse() {
        SelEvent.value = Event(name = SelCourse.value!!.name,type = TYPE_CLASS)
    }

    private fun classBunked() =launch{
        val courseID = SelEvent.value!!.courceid
        val course = Courses.value!!.find { it.ID == courseID }
        course!!.classBunked()
        evaluateResult("update courses",repo.updateCourse(course,true))
        val alert = alerts.value!!.find { it.eventId == DispEvent.value!!.ID }
        evaluateResult("remove alert",repo.removeAlert(alert!!))

    }

    private fun classAttended() =launch{
        val courseID = SelEvent.value!!.courceid
        val course = Courses.value?.find { it.ID == courseID }
        course!!.classAttended()
        evaluateResult("update courses",repo.updateCourse(course,true))
        val alert = alerts.value?.find { it.eventId == DispEvent.value!!.ID }
        evaluateResult("remove alert",repo.removeAlert(alert!!))
    }

    private fun getEventWithId(id : String) = launch {
        when(val res = repo.getEventByID(id)){
            is Result.Value -> SelEvent.value = res.value
            is Result.Error -> errorState.value = ERROR_EVENT_LOAD
        }
    }

    private fun getCourseWithId(id : String) = launch {
        Courses.value!!.find {
            it.ID ==id
        }?.let {
            SelCourse.value = it
            return@launch
        }
        when(val res = repo.getCourseById(id)){
            is Result.Value -> SelCourse.value = res.value
            is Result.Error -> errorState.value = ERROR_COURSE_LOAD
        }
    }

    private fun removeEvent() =launch{
        when(repo.removeEvent(SelEvent.value!!)){
            is Result.Value -> SelEvent.value=null
            is Result.Error -> errorState.value = ERROR_EVENT_REMOVE
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
            is Result.Error -> errorState.value = ERROR_EVENT_LOAD
        }
    }
    private fun getEventsOnDate() = launch {
        when(val repoResult = repo.getEventsIn(Calendar.DAY_OF_MONTH,date = curDate.value!!)){
            is Result.Value -> privateSelectableEvents.value = repoResult.value
            is Result.Error -> errorState.value = ERROR_EVENT_LOAD
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

    override val coroutineContext: CoroutineContext
        get() = uicontext+jobTracker

    private fun evaluateResult (name : String, result: Result<Exception,Unit>){
        when(result){
            is Result.Value -> Log.d(TAG,"$name successfully executed")
            is Result.Error -> Log.e(TAG,"$name failed",result.error)
        }
    }
}

