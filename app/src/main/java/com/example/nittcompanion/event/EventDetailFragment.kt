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
import kotlinx.android.synthetic.main.fragment_event_detail.view.*
import java.util.*

class EventDetailFragment : Fragment() {
    private lateinit var event : Event
    private  lateinit var course: Course
    private lateinit var viewModel: BaseViewModel
    private var alerts = listOf<Alert>()
    private lateinit var mView : View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_detail,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        retainInstance = true
        activity?.let{viewModel = ViewModelProviders.of(requireActivity(), InjectorUtils(requireActivity().application).provideBaseViewModelFactory()).get(BaseViewModel::class.java)}

        course = viewModel.DispCourse.value!!
        event = viewModel.DispEvent.value!!
        setObservations()
        setViews()
        setOnClicks()
    }

    private fun setViews() =activity?.let {

        alerts = viewModel.alerts.value?: listOf()

        val alert = !alerts.none {
            event.ID == it.eventId
        }

        mView.EventDetailName.text = event.name
        mView.Date.text = Calendar.getInstance().getCalEnderWithMillis(event.startDate).getDateInFormat()
        mView.EventStartTime.text = Calendar.getInstance().getCalEnderWithMillis(event.startDate).getTimeInFormat()
        mView.EventEndTime.text = Calendar.getInstance().getCalEnderWithMillis(event.endDate).getTimeInFormat()
        if (event.type == TYPE_CLASS) {
            mView.attendanceTxt.visibility = View.VISIBLE
            mView.EventAttendance.text = requireActivity().resources.getString(R.string.attendenceWithPercent,course.attendance)
            mView.EventAttendance.visibility = View.VISIBLE
            if (alert) {
                mView.DidYouAttendCard.visibility = View.VISIBLE

            } else {
                mView.DidYouAttendCard.visibility = View.GONE
            }
        } else {
            mView.DidYouAttendCard.visibility = View.GONE
            mView.attendanceTxt.visibility = View.GONE
            mView.EventAttendance.visibility = View.GONE
        }
    }

    private fun setObservations() {
        activity?.let { act ->
            viewModel.DispEvent.observe(
                act,
                Observer { dispEvent ->
                    event = dispEvent?:Event()
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

    private fun setOnClicks() =activity?.let {
        mView.CancelEvent.setOnClickListener{
            viewModel.listen(ListenTo.CancellEvent)
            findNavController().popBackStack()
        }
        mView.EditEvent.setOnClickListener{
            val direction = EventDetailFragmentDirections.actionDestinationEventDetailToDestinationEventCreate(null)
            findNavController().navigate(direction)
        }
        mView.EventAttendedPositive.setOnClickListener {
            mView.DidYouAttendCard.visibility = View.GONE
            viewModel.listen(ListenTo.ClassAttended)

        }
        mView.EventAttendedPositive.setOnClickListener {
            mView.DidYouAttendCard.visibility = View.GONE
            viewModel.listen(ListenTo.ClassBunked)
        }
    }

}