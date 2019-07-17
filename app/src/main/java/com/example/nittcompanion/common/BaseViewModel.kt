package com.example.nittcompanion.common

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import com.example.nittcompanion.model.repository.IEventsRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class BaseViewModel(protected val uicontext: CoroutineContext,private val eventRepo:IEventsRepo ) : ViewModel() ,CoroutineScope{

    private val TAG = "BaseViewModel"
    protected val jobTracker: Job

    private val curDate = MutableLiveData<Calendar>()
    val DispDate : LiveData<Calendar> get() = curDate

    private val privateMonthlyEvents = MutableLiveData<List<Event>>()
    val MonthlyEvents : LiveData<List<Event>> get() = privateMonthlyEvents

    private val privateSelectableEvents = MutableLiveData<List<Event>>()
    val SelectableEvents : LiveData<List<Event>> get() = privateSelectableEvents

    private val SelEvent = MutableLiveData<Event>()
    val DispEvent : LiveData<Event> get() = SelEvent

    private val privateCourses  = MutableLiveData<List<Course>>()
    val Courses : LiveData<List<Course>> get() = privateCourses

    private val SelCourse = MutableLiveData<Course>()
    val DispCourse: LiveData<Course> get() = SelCourse

    init {
        SelEvent.value = Event()
        SelCourse.value = Course()
        jobTracker = Job()
        curDate.value = Calendar.getInstance()
    }

    fun listen(listenTo : ListenTo){
        Log.d("TAG","Listen to called")
        when(listenTo){
            is ListenTo.ActivityStarted -> initActivity()
            is ListenTo.NextMonthClicked -> changeMonth(1)
            is ListenTo.PreviourMonthClicked -> changeMonth(-1)
            is ListenTo.DateSelected -> changeDate(listenTo.date)
            is ListenTo.EventClicked -> selectEvent(listenTo.position)
            is ListenTo.ScheduleFragmentstart -> initScheduleFragment()
            is ListenTo.CoursesFragmentstart -> initCoursesFragment()
            is ListenTo.CourseSelected -> selectCourse(listenTo.position)
            is ListenTo.CancellEvent -> removeEvent()
            is ListenTo.RecheduleTo -> rescheduleEvent(listenTo.startDate,listenTo.endDate)
            is ListenTo.ClassAttended -> classAttended()
            is ListenTo.ClassBunked -> classBunked()
        }
    }

    private fun initActivity() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun classBunked() =launch{
        val courseID = SelEvent.value!!.courceid
        when(val result = eventRepo.getCourcesWithID(courseID)){
            is Result.Value -> {result.value.classAttended()
            eventRepo.updateCourse(result.value)}
            is Result.Error -> errorState.value = ERROR_Course_UPDATE
        }

    }

    private fun classAttended() =launch{
        val courseID = SelEvent.value!!.courceid
        when(val result = eventRepo.getCourcesWithID(courseID)){
            is Result.Value -> {result.value.classBunked()
                eventRepo.updateCourse(result.value)}
            is Result.Error -> errorState.value = ERROR_Course_UPDATE
        }
    }

    private fun rescheduleEvent(startDate: Calendar, endDate:  Calendar) =launch{
        SelEvent.value!!.startDate = startDate
        SelEvent.value!!.endDate = endDate
        eventRepo.updateEvent(SelEvent.value!!)
    }

    private fun removeEvent() =launch{
        when(eventRepo.removeEvent(SelEvent.value!!)){
            is Result.Value -> SelEvent.value=null
            is Result.Error -> errorState.value = ERROR_EVENT_REMOVE
        }

    }

    private fun getCourses() = launch {
        when(val result  = eventRepo.getCources()){
            is Result.Value -> privateCourses.value = result.value
            is Result.Error -> errorState.value = ERROR_Courses_LOAD
        }
    }

    private fun selectCourse(position: Int) {
        SelCourse.value = privateCourses.value?.get(position)
    }

    private fun initCoursesFragment() {
        getCourses()
    }

    private fun initScheduleFragment() {
        curDate.value = Calendar.getInstance()
        getEventsInMonth()
    }

    private fun getEventsInMonth() = launch {
        when(val repoResult = eventRepo.getEventsInMonth_Date(value = curDate.value!!.getMonthInFormat())){
            is Result.Value -> privateMonthlyEvents.value = repoResult.value
            is Result.Error -> errorState.value = ERROR_EVENT_LOAD
        }
    }
    private fun getEventsOnDate() = launch {
        when(val repoResult = eventRepo.getEventsInMonth_Date(value = curDate.value!!.getDateInFormat())){
            is Result.Value -> privateMonthlyEvents.value = repoResult.value
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
        SelEvent.value = SelectableEvents.value?.get(pos)
        if (SelEvent.value!!.type == TYPE_CLASS || SelEvent.value!!.type == TYPE_LAB) launch {
            when(val result = eventRepo.getCourcesWithID(DispEvent.value!!.courceid)){
                is Result.Value -> SelCourse.value = result.value
                is Result.Error -> errorState.value = ERROR_Courses_LOAD
            }
        }
    }


    protected val errorState = MutableLiveData<String>()
    val error: LiveData<String> get() = errorState

    override val coroutineContext: CoroutineContext
        get() = uicontext+jobTracker
}