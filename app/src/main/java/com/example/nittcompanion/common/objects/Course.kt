package com.example.nittcompanion.common.objects

import android.util.Log
import com.google.firebase.firestore.Exclude
import java.util.*

data class Course(var name : String = "",
                  var credit : Int = 0,
                  var classEvent : ClassEvent = ClassEvent(islab = false),
                  @set:Exclude @get:Exclude var ID : String = Calendar.getInstance().timeInMillis.toString(),
                  var lastEventCreated : Long = 0,
                  var attended : Int = 0,
                  var notAttended : Int = 0) {


    @get:Exclude
    val attendance : Float get() = attended.toFloat()/(attended+notAttended).toFloat() *100
    @set:Exclude @get:Exclude
    var classToAttend : Int get() = calculateClasses()
    init {
        classToAttend = 0
        calculateClasses()
    }

    fun calculateClasses() : Int{

        val calculateAttendence :(Int,Int) -> Float = { attended, notattended ->
            (attended.toFloat()/(attended.toFloat()+notattended.toFloat())) *100
        }
        var count = 0
        return when {
            attendance == 75f -> 1
            attendance > 75f -> {
                var tempnontattended = notAttended + 0
                tempnontattended +=1
                while(calculateAttendence.invoke(attended,tempnontattended) > 75f){
                    Log.i("Course class","more than 75 ran tempattended : $attended temp not attended $tempnontattended")
                    tempnontattended++
                    count--
                }
                count
            }
            attendance < 75f -> {
                var tempattended = attended
                while(calculateAttendence.invoke(tempattended,notAttended) <= 75f){
                    Log.i("Course class","less than 75 ran tempattended : $tempattended temp not attended $notAttended")
                    tempattended++
                    count++
                }
                count
            }
            else -> 0
        }
    }

    fun classAttended(){
        attended++
        calculateClasses()
    }
    fun classBunked(){
        notAttended++
        calculateClasses()
    }
    fun getRegularClasseOnDay(day : Calendar) = classEvent.getEventOnDay(day,name,ID)

    fun getRegularClasseForWeek(day : Calendar) : List<Event> {
        Log.d("Course","get regular classes for weak called")
        classEvent.getEventOnDay(day,name,ID)
        val cal : Calendar = day.clone() as Calendar
        cal.add(Calendar.DAY_OF_MONTH,(day[Calendar.DAY_OF_WEEK] - Calendar.MONDAY)*-1)
        val events = mutableListOf<Event>()
        while (cal[Calendar.DAY_OF_WEEK] in Calendar.MONDAY..Calendar.FRIDAY){
            Log.d("Course","iterating through getregular classes DOW ${cal[Calendar.DAY_OF_WEEK]} DOM ${cal[Calendar.DAY_OF_MONTH]} ")
            getRegularClasseOnDay(cal)?.let {
                events.add(it)
            }
            cal.add(Calendar.DAY_OF_MONTH,1)
        }
        return events
    }

}