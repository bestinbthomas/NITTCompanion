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
import com.example.nittcompanion.common.objects.Alert
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.Event
import com.google.android.material.snackbar.Snackbar
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
        courses = viewModel.Courses.value?: listOf()

        setObservations()
        setViews()
        setClickListeners()

    }

    override fun onStart() {
        super.onStart()


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

    private fun validateName(): Boolean = if (mView.EventDetailName.editText!!.text.toString().isEmpty()) {
        mView.EventDetailName.error = "please enter a name"
        false
    } else {
        mView.EventDetailName.error = null
        true
    }

    private fun saveEvent() {
        if (!validateName()) return

        if (mView.ImpSwitch.isChecked)
            viewModel.listen(ListenTo.AddAlert(Alert(startDate.timeInMillis, event.ID)))
        viewModel.listen(
            ListenTo.UpdateEvent(
                Event(
                    mView.EventDetailName.editText!!.text.toString(),
                    startDate.timeInMillis,
                    endDate.timeInMillis,
                    mView.typeSpinner.selectedItem.toString(),
                    try {
                        courses[mView.CourseSpinner.selectedItemPosition].ID
                    } catch (e : Exception){
                        ""
                    },
                    event.ID
                )
            )
        )
        createSnackbar("Event Created", Snackbar.LENGTH_SHORT)
        findNavController().popBackStack()

    }


    private fun setViews() = activity?.let {
        startDate.timeInMillis = event.startDate
        endDate.timeInMillis = event.endDate
        val coursenames = mutableListOf<String>()
        courses.forEach {
            coursenames.add(it.name)
        }
        if(event.name.isNotBlank())
            mView.EventDetailName.editText!!.setText(event.name, TextView.BufferType.EDITABLE)
        mView.CourseSpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, coursenames)
        if(event.courceid.isNotBlank()) {
            mView.CourseSpinner.setSelection(courses.indexOf(courses.filter {
                it.ID == event.courceid
            }[0]))
        }
        if(event.type.isNotBlank()) {
            mView.typeSpinner.setSelection(
                when (event.type) {
                    TYPE_CLASS -> 0
                    TYPE_CT -> 1
                    TYPE_ENDSEM -> 2
                    TYPE_ASSIGNMENT -> 3
                    TYPE_LAB -> 4
                    TYPE_OTHER -> 5
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