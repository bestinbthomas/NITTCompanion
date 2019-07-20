package com.example.nittcompanion.common.objects

import android.util.Log
import com.google.firebase.firestore.Exclude
import java.util.*

data class Course(var name : String = "",
                  var credit : Int = 0,
                  var classEvent : ClassEvent = ClassEvent(islab = false),
                  @set:Exclude @get:Exclude var ID : String = Calendar.getInstance().timeInMillis.toString(),
                  var lastEventCreated : Long = Calendar.getInstance().timeInMillis) {


    var notAttended : Int = 0
    var attended : Int = 0
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

}