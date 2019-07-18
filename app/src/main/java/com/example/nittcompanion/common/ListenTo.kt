package com.example.nittcompanion.common

import com.example.nittcompanion.common.objects.Alert
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event

sealed class ListenTo {

    data class EventClicked(val position: Int) : ListenTo()
    data class DateSelected(val date: Int) : ListenTo()
    object PreviousMonthClicked : ListenTo()
    object NextMonthClicked : ListenTo()
    object HomeFragmentStart : ListenTo()
    object ScheduleFragmentstart : ListenTo()
    data class CourseSelected(val position: Int) : ListenTo()
    object CancellEvent : ListenTo()
    object ClassAttended : ListenTo()
    object ClassBunked : ListenTo()
    object AddEventForCourse : ListenTo()
    object RemoveCourse : ListenTo()
    object AddNewEvent : ListenTo()
    object AddNewCourse : ListenTo()
    object CourseDetailStart : ListenTo()
    data class AddAlert(val alert : Alert) : ListenTo()
    data class UpdateCourse(val course: Course) : ListenTo()
    data class UpdateEvent(val event: Event) : ListenTo()
}