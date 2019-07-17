package com.example.nittcompanion.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_event_detail.*

class EventDetailFragment : Fragment() {
    private lateinit var event : Event
    private  lateinit var course: Course
    private lateinit var viewModel: BaseViewModel
    private var alerts = listOf<Alert>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_detail,container,false)
    }

    override fun onStart() {
        super.onStart()
        activity?.let{viewModel = ViewModelProviders.of(requireActivity(), InjectorUtils(requireActivity().application).provideBaseViewModelFactory()).get(BaseViewModel::class.java)}

        val pref = requireActivity().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)

        val alertString  = pref.getString(SHARED_PREF_KEY_ALERTS,null)

        alertString?.let {
            val gson  = Gson()
            alerts = gson.fromJson(it, listOf<Alert>().javaClass)
        }

        course = viewModel.DispCourse.value!!
        event = viewModel.DispEvent.value!!
        setObservations()
        setViews()
        setOnClicks()
    }

    private fun setViews() {

        val alert = !alerts.none {
            event.ID.toLong()/1000 in (it.eventId.toLong()/1000)-2..((it.eventId.toLong()/1000)+2)
        }

        EventDetailName.text = event.name
        Date.text = event.startDate.getDateInFormat()
        EventStartTime.text = event.startDate.getTimeInFormat()
        EventEndTime.text = event.endDate.getTimeInFormat()
        if (event.type == TYPE_CLASS && alert) {
            DidYouAttendCard.visibility = View.VISIBLE
            attendanceTxt.visibility = View.VISIBLE
            EventAttendance.text = requireActivity().resources.getString(R.string.attendenceWithPercent,course.attendance)
            EventAttendance.visibility = View.VISIBLE
        } else {
            DidYouAttendCard.visibility = View.GONE
            attendanceTxt.visibility = View.GONE
            EventAttendance.visibility = View.GONE
        }
    }

    private fun setObservations() {
        activity?.let { act ->
            viewModel.DispEvent.observe(
                act,
                Observer { dispEvent ->
                    event = dispEvent
                    setViews()
                }
            )
            viewModel.DispCourse.observe(
                act,
                Observer {
                    course = it
                    setViews()
                }
            )
        }
    }

    private fun setOnClicks() {
        CancelEvent.setOnClickListener{
            viewModel.listen(ListenTo.CancellEvent)
            findNavController().popBackStack()
        }
        EditEvent.setOnClickListener{
            val direction = EventDetailFragmentDirections.actionDestinationEventDetailToDestinationEventCreate(null)
            findNavController().navigate(direction)
        }
        EventAttendedPositive.setOnClickListener {

            viewModel.listen(ListenTo.ClassAttended)
        }
        EventAttendedPositive.setOnClickListener {
            viewModel.listen(ListenTo.ClassBunked)
        }
    }

    private fun updateAlerts(){
        alerts = alerts.filter {
            event.ID.toLong()/1000 in (it.eventId.toLong()/1000)-2..((it.eventId.toLong()/1000)+2)
        }
        val gson = Gson()
        val alertString = gson.toJson(alerts, listOf<Alert>().javaClass)
        val pref = requireActivity().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(SHARED_PREF_KEY_ALERTS,alertString)
        editor.apply()

    }
}