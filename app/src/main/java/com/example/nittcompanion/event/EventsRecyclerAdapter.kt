package com.example.nittcompanion.event

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.nittcompanion.R
import com.example.nittcompanion.common.*
import com.example.nittcompanion.common.objects.Event
import kotlinx.android.synthetic.main.event_item.view.*
import java.util.*

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
        val millis = events[position].startDate
        holder.time.text = Calendar.getInstance().getCalEnderWithMillis(millis).getTimeInFormat()
        holder.date.text = Calendar.getInstance().getCalEnderWithMillis(millis).getDateInFormat()
        holder.Evntname.text = events[position].name

        when {
            events[position].type in arrayOf(TYPE_CT, TYPE_ENDSEM) -> {
                holder.evetAlert.visibility = View.VISIBLE
                holder.evetAlert.setColorFilter(Color.RED)
            }
            events[position].type == TYPE_ASSIGNMENT -> {
                holder.evetAlert.visibility = View.VISIBLE
                holder.evetAlert.setColorFilter(Color.YELLOW)
            }
            events[position].type == TYPE_CLASS && !events[position].doneUpdate && events[position].endDate <= Calendar.getInstance().timeInMillis -> {
                holder.evetAlert.visibility = View.VISIBLE
                holder.evetAlert.setColorFilter(Color.YELLOW)
            }
            else -> holder.evetAlert.visibility = View.GONE
        }

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
        var evetAlert : ImageView = view.eventAlert
        var date : TextView = view.EventDate
    }
}