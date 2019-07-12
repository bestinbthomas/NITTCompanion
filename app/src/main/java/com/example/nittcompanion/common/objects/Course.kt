package com.example.nittcompanion.common.objects

data class Course(val name : String,val credit : Int) {
    private var notAttended : Int = 0
    private var attended : Int = 0
    var attendance : Float
    var classToAttend : Int
    init {
        attendance = 0f
        classToAttend = 0
        calculateClasses()
    }

    fun calculateClasses(){
        var tempattended = attended
        var tempnontattended = notAttended
        var count : Int = 0
        attendance = (notAttended.toFloat()/(attended+notAttended).toFloat())*100
        classToAttend = if (attendance == 75f)
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

}