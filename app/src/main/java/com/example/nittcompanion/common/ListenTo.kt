package com.example.nittcompanion.common

import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import java.util.*

sealed class ListenTo {

    object ActivityStarted : ListenTo()
    data class EventClicked(val position: Int) : ListenTo()
    data class DateSelected(val date: Int) : ListenTo()
    object PreviourMonthClicked : ListenTo()
    object NextMonthClicked : ListenTo()
    object ScheduleFragmentstart : ListenTo()
    object CoursesFragmentstart : ListenTo()
    data class CourseSelected(val position: Int) : ListenTo()
    object CancellEvent : ListenTo()
    data class RecheduleTo(val startDate: Calendar,val endDate: Calendar) : ListenTo()
    object ClassAttended : ListenTo()
    object ClassBunked : ListenTo()
    object AddEventForCourse : ListenTo()
    object RemoveCourse : ListenTo()
    object AddNewEvent : ListenTo()
    object AddNewCourse : ListenTo()
    data class UpdateCourse(val course: Course) : ListenTo()
    data class UpdateEvent(val event: Event) : ListenTo()
}