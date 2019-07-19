package com.example.nittcompanion.calender

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.nittcompanion.R
import com.example.nittcompanion.common.*
import com.example.nittcompanion.common.objects.Alert
import com.example.nittcompanion.common.objects.Event
import java.util.*


class CalGridAdapter(
    val context: Context,
    var date: Calendar,
    var events: List<Event> = listOf(),
    var alerts: List<Alert> = listOf()
) : BaseAdapter() {

    //private var tempdate = date.copy()
    private val noOfDays = date.getActualMaximum(Calendar.DAY_OF_MONTH)
    private val bufferDate get() = date.getbeforeFirstDayOfMonth()

    init {
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view : View = convertView?:LayoutInflater.from(context).inflate(R.layout.calender_item,parent,false)
        val calitemroot = view.findViewById<ConstraintLayout>(R.id.calItemConstraint)
        val calback = view.findViewById<ImageView>(R.id.currBack)
        val dispdate = view.findViewById<TextView>(R.id.calDate)
        val cautionbar1 = view.findViewById<ImageView>(R.id.reddash)
        val cautionbar2 = view.findViewById<ImageView>(R.id.yellowdash)
        val cautiondot = view.findViewById<ImageView>(R.id.yellowdot)

        if (position < bufferDate){
            calitemroot.visibility = View.INVISIBLE
        }
        else{
            val disp = position-bufferDate+1
            calback.visibility = View.INVISIBLE
            cautiondot.visibility = View.INVISIBLE
            cautionbar2.visibility = View.INVISIBLE
            cautionbar1.visibility = View.INVISIBLE
            dispdate.text = disp.toString()
            dispdate.setTextColor(Color.WHITE)
            if (disp==date.get(Calendar.DAY_OF_MONTH)){
                dispdate.setTextColor(Color.BLACK)
                calback.visibility = View.VISIBLE
            }
            events.forEach {
                if (Calendar.getInstance().getCalEnderWithMillis(it.startDate)[Calendar.DAY_OF_MONTH] == disp) {
                    if (it.type in arrayOf(TYPE_ASSIGNMENT, TYPE_ENDSEM, TYPE_CT))
                        cautionbar1.visibility = View.VISIBLE
                    else if (it.type == TYPE_OTHER)
                        cautionbar2.visibility = View.VISIBLE
                }
            }

            alerts.forEach {
                date = Calendar.getInstance().getCalEnderWithMillis(it.timeinmillis)
                if (date[Calendar.DATE] == disp && disp <= Calendar.getInstance()[Calendar.DATE])
                    cautiondot.visibility = View.VISIBLE
            }
        }

        return view

    }

    override fun getItem(position: Int): Any {
        return position-bufferDate
    }

    override fun getItemId(position: Int): Long {
        return (position-bufferDate+1).toLong()
    }

    override fun getCount(): Int {
        return noOfDays+bufferDate
    }

    fun updateDate(date : Calendar){
        this.date = date

    }

    fun updateEvents(events: List<Event>){
        this.events = events
    }

    fun updateAlerts(alerts: List<Alert>){
        this.alerts = alerts
    }



}