package com.example.nittcompanion.common.objects

import com.example.nittcompanion.common.TYPE_CLASS
import com.example.nittcompanion.common.TYPE_LAB
import java.util.*

open class ClassEvent(var classes: HashMap<String, Int> = hashMapOf(),var islab : Boolean = false) {


    private fun getStartTime(time: Calendar): Long {
        val slot = classes[time[Calendar.DAY_OF_WEEK].toString()]
        if (slot!=-1) {
            when (slot) {
                1 -> {
                    time.set(Calendar.HOUR_OF_DAY, 8)
                    time.set(Calendar.MINUTE, 30)
                    time.set(Calendar.SECOND,0)
                }
                2 -> {
                    time.set(Calendar.HOUR_OF_DAY, 9)
                    time.set(Calendar.MINUTE, 20)
                    time.set(Calendar.SECOND,0)
                }
                3 -> {
                    time.set(Calendar.HOUR_OF_DAY, 10)
                    time.set(Calendar.MINUTE, 30)
                    time.set(Calendar.SECOND,0)
                }
                4 -> {
                    time.set(Calendar.HOUR_OF_DAY, 11)
                    time.set(Calendar.MINUTE, 20)
                    time.set(Calendar.SECOND,0)
                }
                5 -> {
                    time.set(Calendar.HOUR_OF_DAY, 1)
                    time.set(Calendar.MINUTE, 30)
                    time.set(Calendar.SECOND,0)
                }
                6 -> {
                    time.set(Calendar.HOUR_OF_DAY, 2)
                    time.set(Calendar.MINUTE, 20)
                    time.set(Calendar.SECOND,0)
                }
                7 -> {
                    time.set(Calendar.HOUR_OF_DAY, 3)
                    time.set(Calendar.MINUTE, 30)
                    time.set(Calendar.SECOND,0)
                }
                8 -> {
                    time.set(Calendar.HOUR_OF_DAY, 4)
                    time.set(Calendar.MINUTE, 20)
                    time.set(Calendar.SECOND,0)

                }

            }
        }
        return time.timeInMillis
    }

    private fun getLabStartTime(time: Calendar): Long {
        val slot = classes[time[Calendar.DAY_OF_WEEK].toString()]
        if (slot!=-1) {
            when (slot) {
                1 -> {
                    time.set(Calendar.HOUR_OF_DAY, 8)
                    time.set(Calendar.MINUTE, 30)
                    time.set(Calendar.SECOND,0)
                }
                2 -> {
                    time.set(Calendar.HOUR_OF_DAY, 1)
                    time.set(Calendar.MINUTE, 30)
                    time.set(Calendar.SECOND,0)
                }

            }
        }
        return time.timeInMillis
    }

    private fun getEndTime(time: Calendar): Long {
        val slot = classes[time[Calendar.DAY_OF_WEEK].toString()]
        if (slot!=-1) {
            when (slot) {
                1 -> {
                    time.set(Calendar.HOUR_OF_DAY, 9)
                    time.set(Calendar.MINUTE, 20)
                    time.set(Calendar.SECOND,0)
                }
                2 -> {
                    time.set(Calendar.HOUR_OF_DAY, 10)
                    time.set(Calendar.MINUTE, 10)
                    time.set(Calendar.SECOND,0)
                }
                3 -> {
                    time.set(Calendar.HOUR_OF_DAY, 11)
                    time.set(Calendar.MINUTE, 20)
                    time.set(Calendar.SECOND,0)
                }
                4 -> {
                    time.set(Calendar.HOUR_OF_DAY, 12)
                    time.set(Calendar.MINUTE, 10)
                    time.set(Calendar.SECOND,0)
                }
                5 -> {
                    time.set(Calendar.HOUR_OF_DAY, 2)
                    time.set(Calendar.MINUTE, 20)
                    time.set(Calendar.SECOND,0)
                }
                6 -> {
                    time.set(Calendar.HOUR_OF_DAY, 3)
                    time.set(Calendar.MINUTE, 10)
                    time.set(Calendar.SECOND,0)
                }
                7 -> {
                    time.set(Calendar.HOUR_OF_DAY, 4)
                    time.set(Calendar.MINUTE, 20)
                    time.set(Calendar.SECOND,0)
                }
                8 -> {
                    time.set(Calendar.HOUR_OF_DAY, 5)
                    time.set(Calendar.MINUTE, 10)
                    time.set(Calendar.SECOND,0)
                }

            }
        }
        return time.timeInMillis
    }

    private fun getLabEndTime(time: Calendar): Long {
        val slot = classes[time[Calendar.DAY_OF_WEEK].toString()]
        if (slot!=-1) {
            when (slot) {
                1 -> {
                    time.set(Calendar.HOUR_OF_DAY, 12)
                    time.set(Calendar.MINUTE, 10)
                    time.set(Calendar.SECOND,0)
                }
                2 -> {
                    time.set(Calendar.HOUR_OF_DAY, 5)
                    time.set(Calendar.MINUTE, 20)
                    time.set(Calendar.SECOND,0)
                }


            }
        }
        return time.timeInMillis
    }

    fun getEventOnDay(day: Calendar, courseName: String, courseId: String) =
        if (classes.containsKey(day[Calendar.DAY_OF_WEEK].toString()) && classes[day[Calendar.DAY_OF_WEEK].toString()] != 0) {
            val startDate = if (islab) getLabStartTime(day) else getStartTime(day)
            val enddate = if(islab) getLabEndTime(day) else getEndTime(day)
            Event(
                "$courseName ${if (islab) "Lab" else "Class"} ",
                startDate,
                enddate,
                if (islab) TYPE_LAB else TYPE_CLASS,
                courseId,
                doneUpdate = false,
                imp = false
            )
        }
        else null

}

