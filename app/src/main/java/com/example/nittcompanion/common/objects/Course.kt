package com.example.nittcompanion.common.objects

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.Exclude
import java.util.*

data class Course(var name : String = "",
                  var credit : Int = 0,
                  var classEvent : ClassEvent = ClassEvent(islab = false),
                  @Exclude var ID : String = Calendar.getInstance().timeInMillis.toString()) {


    var notAttended : Int = 0
    var attended : Int = 0
    @set:Exclude @get:Exclude
    var attendance : Float
    @set:Exclude @get:Exclude
    var classToAttend : MutableLiveData<Int> = MutableLiveData()
    init {
        attendance = 0f
        classToAttend.value = 0
        calculateClasses()
    }

    fun calculateClasses(){
        var tempattended = attended
        var tempnontattended = notAttended
        var count = 0
        attendance = (notAttended.toFloat()/(attended+notAttended).toFloat())*100
        classToAttend.value = if (attendance == 75f)
                            0
                        else if(attendance > 75f )
                        {
                            while ((tempattended /(tempattended+(tempnontattended++)))*100 >= 75){
                                count--
                            }
                            count
                        }
                        else{
                            while ((++tempattended/(tempattended+tempnontattended))*100 <= 75){
                                count++
                            }
                            count
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