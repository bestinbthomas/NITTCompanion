package com.example.nittcompanion.common.objects

import com.example.nittcompanion.common.TYPE_CLASS
import java.util.*

data class ClassEvent(var classes: HashMap<Int, Int> = hashMapOf()) {


    private fun getStartTime(time: Calendar): Calendar {
        val slot = classes[time[Calendar.DAY_OF_WEEK]]
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
        return time
    }

    private fun getEndTime(time: Calendar): Calendar {
        val slot = classes[time[Calendar.DAY_OF_WEEK]]
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
        return time
    }

    fun getEventOnDay(day: Calendar, courseName: String, courseId: String) =
        if (classes[day[Calendar.DAY_OF_WEEK]] != 0)
            Event("$courseName Class", getStartTime(day), getEndTime(day), TYPE_CLASS, courseId)
        else null
}