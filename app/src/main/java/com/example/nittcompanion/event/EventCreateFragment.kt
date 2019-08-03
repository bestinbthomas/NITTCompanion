package com.example.nittcompanion.event

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.nittcompanion.R
import com.example.nittcompanion.common.*
import com.example.nittcompanion.common.factoryAndInjector.InjectorUtils
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import com.example.nittcompanion.notification.NotifyAlert
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_event_create.*
import kotlinx.android.synthetic.main.fragment_event_create.view.*
import java.util.*

class EventCreateFragment : Fragment() {
    private lateinit var viewModel: BaseViewModel
    private lateinit var courses: List<Course>
    private lateinit var event: Event
    private val startDate = Calendar.getInstance()
    private val endDate = Calendar.getInstance()
    private lateinit var mView: View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        retainInstance = true
        activity?.let {
            viewModel = ViewModelProviders.of(
                requireActivity(),
                InjectorUtils(requireActivity().application).provideBaseViewModelFactory()
            ).get(BaseViewModel::class.java)
        }

        event = viewModel.DispEvent.value!!
        courses = viewModel.Courses.value ?: listOf()

        setObservations()
        setViews()
        setClickListeners()

    }

    private fun setClickListeners() = activity?.let {
        mView.SaveEvent.setOnClickListener {
            saveEvent()
        }
        mView.Date.setOnClickListener {
            val datePickerlistener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                startDate[Calendar.YEAR] = year
                startDate[Calendar.MONTH] = month
                startDate[Calendar.DAY_OF_MONTH] = dayOfMonth
                endDate[Calendar.YEAR] = year
                endDate[Calendar.MONTH] = month
                endDate[Calendar.DAY_OF_MONTH] = dayOfMonth
                mView.Date.text = startDate.getDateInFormat()
                updateEndTime()
            }
            DatePickerDialog(
                requireContext(),
                datePickerlistener,
                startDate[Calendar.YEAR],
                startDate[Calendar.MONTH],
                startDate[Calendar.DAY_OF_MONTH]
            ).show()
        }
        mView.EventStartTime.setOnClickListener {
            val startTimePickerListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                startDate[Calendar.HOUR_OF_DAY] = hourOfDay
                startDate[Calendar.MINUTE] = minute
                startDate[Calendar.SECOND] = 5
                mView.EventStartTime.text = startDate.getTimeInFormat()
                updateEndTime()
            }
            TimePickerDialog(
                requireContext(),
                startTimePickerListener,
                startDate[Calendar.HOUR_OF_DAY],
                startDate[Calendar.MINUTE],
                true
            ).show()
        }
        mView.EventEndTime.setOnClickListener {
            val endTimePickerListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                endDate[Calendar.HOUR_OF_DAY] = hourOfDay
                endDate[Calendar.MINUTE] = minute
                endDate[Calendar.SECOND] = 5
                mView.EventEndTime.text = endDate.getTimeInFormat()
            }
            TimePickerDialog(
                requireContext(),
                endTimePickerListener,
                endDate[Calendar.HOUR_OF_DAY],
                endDate[Calendar.MINUTE],
                true
            ).show()
        }
    }

    private fun updateEndTime() {

         val time =startDate.clone() as Calendar
        Log.e("EventCreate","update event called with start time ${time.getTimeInFormat()}")
        when(event.type){
            TYPE_CLASS -> {
                time.add(Calendar.MINUTE,50)
            }

            TYPE_LAB -> {
                time.add(Calendar.HOUR_OF_DAY,3)
            }

            TYPE_CT -> {
                time.add(Calendar.HOUR_OF_DAY,1)
            }

            TYPE_ENDSEM -> {
                time.add(Calendar.HOUR_OF_DAY,3)
            }
            TYPE_ASSIGNMENT -> {

            }

            TYPE_OTHER -> {
                time.add(Calendar.HOUR_OF_DAY,1)
            }
        }
        event.endDate = time.timeInMillis
        mView.EventEndTime.text = time.getTimeInFormat()
    }

    private fun validateName(): Boolean = if (mView.EventDetailName.editText!!.text.toString().isEmpty()) {
        mView.EventDetailName.error = "please enter a name"
        false
    } else {
        mView.EventDetailName.error = null
        true
    }

    private fun saveEvent() {
        if (!validateName()) return

        event.name = mView.EventDetailName.editText!!.text.toString()
        event.courceid = try {
            courses[mView.CourseSpinner.selectedItemPosition - 1].ID
        } catch (e: Exception) {
            ""
        }
        event.type = mView.typeSpinner.selectedItem.toString()
        event.imp = ImpSwitch.isChecked
        if (mView.ImpSwitch.isChecked || event.type in arrayOf(TYPE_CLASS, TYPE_LAB)) {

            val notifyIntent = Intent(requireActivity().applicationContext,NotifyAlert::class.java)
            notifyIntent.putExtra(KEY_EVENT_ID, event.ID)
            notifyIntent.putExtra(KEY_EVENT_NAME, event.name)
            notifyIntent.putExtra(KEY_COURSE_ID,event.courceid)
            notifyIntent.putExtra(KEY_IS_CLASS,event.type in arrayOf(TYPE_CLASS, TYPE_LAB))
            if(PendingIntent.getBroadcast(requireActivity().applicationContext,event.ID.takeLast(5).toInt(),notifyIntent,
                    PendingIntent.FLAG_NO_CREATE)==null) {
                val notifyPendingIntent = PendingIntent.getBroadcast(
                    requireActivity().applicationContext, event.ID.takeLast(5).toInt(), notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                val alarmManager =
                    requireActivity().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val time = if(event.type in arrayOf(TYPE_CLASS, TYPE_LAB))event.endDate else event.startDate - (30*60*1000)
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, notifyPendingIntent)
            }
        }
        viewModel.listen(
            ListenTo.UpdateEvent(
                Event(
                    event.name,
                    startDate.timeInMillis,
                    endDate.timeInMillis,
                    event.type,
                    event.courceid,
                    event.ID,
                    event.imp
                )
            )
        )
        createSnackbar("Event Created", Snackbar.LENGTH_SHORT)
        findNavController().popBackStack()

    }

    private fun setViews() = activity?.let {
        ImpSwitch.isChecked = event.imp
        startDate.timeInMillis = event.startDate
        endDate.timeInMillis = event.endDate

        if(!it.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).getBoolean(KEY_CR,false)){
            mView.typeSpinner.visibility = View.GONE
            mView.typeSpinnerLabel.visibility = View.GONE
            mView.CourseSpinner.visibility = View.GONE
            mView.CourseSpinnerLabel.visibility = View.GONE
        }
        val coursenames = mutableListOf<String>()
        coursenames.add("None")

        courses.forEach {
            coursenames.add(it.name)
        }
        if (event.name.isNotBlank())
            mView.EventDetailName.editText!!.setText(event.name, TextView.BufferType.EDITABLE)
        mView.CourseSpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, coursenames)
        if (event.courceid.isNotBlank()) {
            mView.CourseSpinner.setSelection(courses.indexOf(courses.find {
                it.ID == event.courceid
            }) + 1)
        }
        mView.typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                event.type = TYPE_OTHER
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                event.type = parent?.getItemAtPosition(position).toString()
                Log.d("EventCreate","event type set to ${event.type}")
                updateEndTime()
            }

        }
        if (event.type.isNotBlank()) {
            mView.typeSpinner.setSelection(
                when (event.type) {
                    TYPE_OTHER -> 0
                    TYPE_CLASS -> 1
                    TYPE_LAB -> 2
                    TYPE_ASSIGNMENT -> 3
                    TYPE_CT -> 4
                    TYPE_ENDSEM -> 5
                    else -> -1
                }
            )
        }
        mView.Date.text = startDate.getDateInFormat()
        mView.EventStartTime.text = startDate.getTimeInFormat()
        mView.EventEndTime.text = endDate.getTimeInFormat()
    }

    private fun setObservations() {
        activity?.let { act ->
            viewModel.Courses.observe(
                act,
                Observer { crs ->
                    courses = crs
                    setViews()
                }
            )
            viewModel.DispEvent.observe(
                act,
                Observer { ent ->
                    event = ent
                    setViews()
                }
            )
        }
    }
}
