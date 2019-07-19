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
import kotlinx.android.synthetic.main.fragment_event_detail.*
import java.util.*

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

        course = viewModel.DispCourse.value!!
        event = viewModel.DispEvent.value!!
        setObservations()
        setViews()
        setOnClicks()
    }

    private fun setViews() {

        alerts = viewModel.alerts.value?: listOf()

        val alert = !alerts.none {
            event.ID == it.eventId
        }

        EventDetailName.text = event.name
        Date.text = Calendar.getInstance().getCalEnderWithMillis(event.startDate).getDateInFormat()
        EventStartTime.text = Calendar.getInstance().getCalEnderWithMillis(event.startDate).getTimeInFormat()
        EventEndTime.text = Calendar.getInstance().getCalEnderWithMillis(event.endDate).getTimeInFormat()
        if (event.type == TYPE_CLASS) {
            attendanceTxt.visibility = View.VISIBLE
            EventAttendance.text = requireActivity().resources.getString(R.string.attendenceWithPercent,course.attendance)
            EventAttendance.visibility = View.VISIBLE
            if (alert) {
                DidYouAttendCard.visibility = View.VISIBLE

            } else {
                DidYouAttendCard.visibility = View.GONE
            }
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
            viewModel.alerts.observe(
                act,
                Observer {
                    alerts = it
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
            DidYouAttendCard.visibility = View.GONE
            viewModel.listen(ListenTo.ClassAttended)

        }
        EventAttendedPositive.setOnClickListener {
            DidYouAttendCard.visibility = View.GONE
            viewModel.listen(ListenTo.ClassBunked)
        }
    }

}