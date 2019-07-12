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
import androidx.recyclerview.widget.RecyclerView
import com.example.nittcompanion.R
import com.example.nittcompanion.common.copy
import com.example.nittcompanion.common.getbeforeFirstDayOfMonth
import com.example.nittcompanion.common.objects.Event
import java.util.*
import kotlin.collections.ArrayList


class CalGridAdapter(val context : Context,var date : Calendar,var events : List<Event> = listOf()) : BaseAdapter() {

    var tempdate = date.copy()
    val NoOfDays = date.getActualMaximum(Calendar.DAY_OF_MONTH)
    val bufferDate = tempdate.getbeforeFirstDayOfMonth()

    init {
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view : View = LayoutInflater.from(context).inflate(R.layout.calender_item,parent,false)
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
            if (disp==date.get(Calendar.DAY_OF_MONTH)){
                dispdate.setTextColor(Color.BLACK)
                dispdate.text = disp.toString()
                calback.visibility = View.VISIBLE
                cautionbar1.visibility=View.VISIBLE
            }
            else{
                dispdate.setTextColor(Color.WHITE)
                dispdate.text = disp.toString()
                calback.visibility = View.INVISIBLE
                cautiondot.visibility = View.VISIBLE
                cautionbar2.visibility = View.VISIBLE
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
        return NoOfDays+bufferDate
    }



}