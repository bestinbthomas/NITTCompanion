package com.example.nittcompanion.common

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

val sdfDate :SimpleDateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault(Locale.Category.FORMAT))
val sdfTime :SimpleDateFormat = SimpleDateFormat("h : mm a", Locale.getDefault(Locale.Category.FORMAT))

fun Calendar.copy() : Calendar {
    val cal : Calendar = Calendar.getInstance()
    cal.timeInMillis = this.timeInMillis
    return cal
}
fun Calendar.getDateInFormat() : String = sdfDate.format(this.time)
fun Calendar.getTimeInFormat() : String = sdfTime.format(this.time)
fun Calendar.getMonthInFormat() : String = "${this.get(Calendar.MONTH) + 1} / ${this.get(Calendar.YEAR)}"
fun Calendar.getCalEnderWithMillis(millis :Long) : Calendar {
    this.timeInMillis = millis
    return this
}
fun Calendar.getbeforeFirstDayOfMonth() : Int {
    val temp = this.copy()
    while(temp.get(Calendar.DAY_OF_MONTH)>1){
        temp.add(Calendar.DAY_OF_MONTH,-1)
    }
    return temp.get(Calendar.DAY_OF_WEEK)-1
}

fun Fragment.createSnackbar(msg : String, duration :Int, actionTitle : String? = null,onClick:(View) -> Unit = {Unit}){
    val snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content),msg,duration)
        if(actionTitle!=null&&onClick!={Unit})snackbar.setAction(actionTitle,onClick)
    snackbar.show()
}

fun AppCompatActivity.createSnackbar(msg : String, duration :Int, actionTitle : String? = null,onClick:(View) -> Unit = {Unit}){
    val snackbar = Snackbar.make(this.findViewById(android.R.id.content),msg,duration)
    if(actionTitle!=null&&onClick!={Unit})snackbar.setAction(actionTitle,onClick)
    snackbar.show()
}



