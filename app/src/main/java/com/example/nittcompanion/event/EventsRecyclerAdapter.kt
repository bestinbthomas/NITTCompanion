package com.example.nittcompanion.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.nittcompanion.R
import com.example.nittcompanion.common.ListenTo
import com.example.nittcompanion.common.getTimeInFormat
import com.example.nittcompanion.common.objects.Event
import kotlinx.android.synthetic.main.event_item.view.*

class EventsRecyclerAdapter(private var events: List<Event>, val eventClickListener: MutableLiveData<ListenTo> = MutableLiveData()) : RecyclerView.Adapter<EventsRecyclerAdapter.MyHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val inflator = LayoutInflater.from(parent.context)
        return MyHolder(
            inflator.inflate(
                R.layout.event_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.time.text = events[position].startDate.getTimeInFormat()
        holder.Evntname.text = events[position].name

        holder.itemView.setOnClickListener {
            eventClickListener.value = ListenTo.EventClicked(position)
        }
    }

    fun updateEvents(events: List<Event>){
        this.events = events
        notifyDataSetChanged()
    }
    class MyHolder(view: View) : RecyclerView.ViewHolder(view){
        var time: TextView = view.EventTime
        var Evntname: TextView = view.EventName
    }
}