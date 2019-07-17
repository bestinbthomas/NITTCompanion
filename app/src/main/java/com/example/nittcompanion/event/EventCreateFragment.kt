package com.example.nittcompanion.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_event_create.*
import java.util.*

class EventCreateFragment : Fragment() {
    private lateinit var viewModel: BaseViewModel
    private lateinit var courses : List<Course>
    private lateinit var event: Event
    private val startDate = Calendar.getInstance()
    private val endDate = Calendar.getInstance()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_create,container,false)
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            viewModel = ViewModelProviders.of(
                requireActivity(),
                InjectorUtils(requireActivity().application).provideBaseViewModelFactory()).get(BaseViewModel::class.java)
        }

        event = viewModel.DispEvent.value!!
        courses = viewModel.Courses.value!!
        setObservations()
        setViews()
        setClickListeners()

    }

    private fun setClickListeners() {
        SaveEvent.setOnClickListener {
            saveEvent()
        }
        Date.setOnClickListener {
            val datePickerlistener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                startDate[Calendar.YEAR] = year
                startDate[Calendar.MONTH] = month
                startDate[Calendar.DAY_OF_MONTH] = dayOfMonth
                endDate[Calendar.YEAR] = year
                endDate[Calendar.MONTH] = month
                endDate[Calendar.DAY_OF_MONTH] = dayOfMonth
                Date.text = startDate.getDateInFormat()
            }
            DatePickerDialog(
                requireContext(),
                datePickerlistener,
                startDate[Calendar.YEAR],
                startDate[Calendar.MONTH],
                startDate[Calendar.DAY_OF_MONTH]
            ).show()
        }
        EventStartTime.setOnClickListener {
            val startTimePickerListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                startDate[Calendar.HOUR_OF_DAY] = hourOfDay
                startDate[Calendar.MINUTE] = minute
                startDate[Calendar.SECOND] = 5
                EventStartTime.text = startDate.getTimeInFormat()
            }
            TimePickerDialog(requireContext(),
                startTimePickerListener,
                startDate[Calendar.HOUR_OF_DAY],
                startDate[Calendar.MINUTE],
                true)
        }
        EventEndTime.setOnClickListener {
            val endTimePickerListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                endDate[Calendar.HOUR_OF_DAY] = hourOfDay
                endDate[Calendar.MINUTE] = minute
                endDate[Calendar.SECOND] = 5
                EventEndTime.text = endDate.getTimeInFormat()
            }
            TimePickerDialog(requireContext(),
                endTimePickerListener,
                endDate[Calendar.HOUR_OF_DAY],
                endDate[Calendar.MINUTE],
                true)
        }
    }

    private fun validateName(): Boolean = if (EventDetailName.editText!!.text.toString().isEmpty()) {
        EventDetailName.error = "please enter a name"
        true
    } else {
        EventDetailName.error = null
        false
    }

    private fun saveEvent() {
        if (!validateName() ) return
        viewModel.listen(
            ListenTo.UpdateEvent(
                Event(
                    EventDetailName.editText!!.text.toString(),
                    startDate,
                    endDate,
                    typeSpinner.selectedItem.toString(),
                    courses[CourseSpinner.selectedItemPosition].ID,
                    event.ID
                )
            )
        )
        createSnackbar("Event Created", Snackbar.LENGTH_SHORT)
        findNavController().popBackStack()
        TODO("call broadcast for important")
    }


    private fun setViews() {
        val coursenames = mutableListOf<String>()
        courses.forEach {
            coursenames.add(it.name)
        }
        EventDetailName.editText!!.setText(event.name,TextView.BufferType.EDITABLE)
        CourseSpinner.adapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_dropdown_item,coursenames)
        Date.text = startDate.getDateInFormat()
        EventStartTime.text = startDate.getTimeInFormat()
        EventEndTime.text = endDate.getTimeInFormat()
    }

    private fun setObservations() {
        activity?.let{act ->
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