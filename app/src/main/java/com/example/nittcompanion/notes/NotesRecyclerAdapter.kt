package com.example.nittcompanion.notes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.nittcompanion.R
import com.example.nittcompanion.common.NOTE_UNLOADING
import com.example.nittcompanion.common.NOTE_UPLOADED_SUCESS

class NotesRecyclerAdapter(private val context : Context,private val notes : MutableList<Note>,val noteSelectLiveData: MutableLiveData<Int> = MutableLiveData(),val noteLongPressLiveData: MutableLiveData<Int> = MutableLiveData()) :RecyclerView.Adapter<NotesRecyclerAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflator = LayoutInflater.from(parent.context)
        return Holder(inflator.inflate(R.layout.note_item,parent,false))
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = notes[position].name
        holder.progressicon.setImageDrawable(
            when {
                notes[position].added== NOTE_UPLOADED_SUCESS -> context.getDrawable(R.drawable.ic_done)
                notes[position].added== NOTE_UNLOADING -> context.getDrawable(R.drawable.ic_progress)
                else -> context.getDrawable(R.drawable.ic_error)
            }
        )
        holder.layoutCard.setOnClickListener {
            noteSelectLiveData.value = position
        }
        holder.layoutCard.setOnLongClickListener {
            noteLongPressLiveData.value = position
            true
        }
    }

    class Holder(view : View) : RecyclerView.ViewHolder(view){
        val name : TextView = view.findViewById(R.id.NoteName)
        val layoutCard : CardView = view.findViewById(R.id.NoteItemForeground)
        val progressicon : ImageView = view.findViewById(R.id.ProgressIcon)
    }
}