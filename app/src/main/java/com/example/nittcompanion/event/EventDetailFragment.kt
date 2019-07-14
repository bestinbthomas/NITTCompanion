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
import com.example.nittcompanion.common.objects.Event
import kotlinx.android.synthetic.main.fragment_event_detail.*

class EventDetailFragment : Fragment() {
    private lateinit var event : Event
    private lateinit var viewModel: BaseViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_detail,container,false)
    }

    override fun onStart() {
        super.onStart()
        activity?.let{viewModel = ViewModelProviders.of(requireActivity(), InjectorUtils(requireActivity().application).provideBaseViewModelFactory()).get(BaseViewModel::class.java)}

        event = viewModel.DispEvent.value!!
        setObservations()
        setViews()
        setOnClicks()
    }

    private fun setViews() {
        EventDetailName.text = event.name
        Date.text = event.startDate.getDateInFormat()
        EventStartTime.text = event.startDate.getTimeInFormat()
        EventEndTime.text = event.endDate.getTimeInFormat()
        if (event.type == TYPE_CLASS) DidYouAttendCard.visibility = View.VISIBLE
        else DidYouAttendCard.visibility = View.GONE
    }

    private fun setObservations() {
        activity?.let { act ->
            viewModel.DispEvent.observe(
                act,
                Observer { dispEvent ->
                    event = dispEvent
                }
            )
        }
    }

    private fun setOnClicks() {
        CancelEvent.setOnClickListener{
            viewModel.listen(ListenTo.CancellEvent)
            findNavController().popBackStack()
        }
        RescheduleEvent.setOnClickListener{
            TODO("open reschedule dialogue")
        }
        EventAttendedPositive.setOnClickListener {
            viewModel.listen(ListenTo.ClassAttended)
        }
        EventAttendedPositive.setOnClickListener {
            viewModel.listen(ListenTo.ClassBunked)
        }
    }
}