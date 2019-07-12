package com.example.nittcompanion.common

sealed class ListenTo {

    data class EventClicked(val position: Int) : ListenTo()
    data class DateSelected(val date: Int) : ListenTo()
    object PreviourMonthClicked : ListenTo()
    object NextMonthClicked : ListenTo()
    object ScheduleFragmentstart : ListenTo()
    object CoursesFragmentstart : ListenTo()
    data class CourseSelected(val position: Int) : ListenTo()
}