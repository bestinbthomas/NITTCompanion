package com.example.nittcompanion.courses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.nittcompanion.R
import com.example.nittcompanion.common.ListenTo
import com.example.nittcompanion.common.objects.Course
import kotlinx.android.synthetic.main.course_item.view.*

class CourcesRecyclerAdapter(private val context: Context, var courses : List<Course>, val eventSelectListen: MutableLiveData<ListenTo> = MutableLiveData()) : RecyclerView.Adapter<CourcesRecyclerAdapter.MyHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val inflator = LayoutInflater.from(parent.context)
        return MyHolder(inflator.inflate(R.layout.course_item,parent,false))
    }

    override fun getItemCount(): Int = courses.size

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = courses[position].name
        holder.attendance.text = context.resources.getString(R.string.attendenceWithPercent,courses[position].attendance)
        val classtoattend = courses[position].classToAttend.value!!
        holder.status.text = if(classtoattend<0) context.resources.getString(R.string.courseStatusBunk,(classtoattend*-1)) else context.resources.getString(R.string.courseStatusAttend,classtoattend)
        holder.root.setOnClickListener {
            eventSelectListen.value = ListenTo.CourseSelected(position)
        }
    }

    fun updateCources(courses : List<Course>){
        this.courses = courses
        notifyDataSetChanged()
    }

    class MyHolder(view:View) : RecyclerView.ViewHolder(view){
        val name : TextView = view.cource_Name
        val status : TextView = view.course_Status
        val attendance : TextView = view.course_Attendance
        val root : CardView = view.CourseRootCard
    }
}
