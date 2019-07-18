package com.example.nittcompanion.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nittcompanion.R
import com.example.nittcompanion.common.BaseViewModel
import com.example.nittcompanion.common.HOME_COURSE_ID
import com.example.nittcompanion.common.HOME_COURSE_NAME
import com.example.nittcompanion.common.ListenTo
import com.example.nittcompanion.common.factoryAndInjector.InjectorUtils
import com.example.nittcompanion.common.objects.Event
import com.example.nittcompanion.event.EventsRecyclerAdapter
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    private lateinit var viewModel: BaseViewModel
    private lateinit var adapter: EventsRecyclerAdapter
    private var events = listOf<Event>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home,container,false);
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            viewModel = ViewModelProviders.of(
                requireActivity(),
                InjectorUtils(requireActivity().application).provideBaseViewModelFactory()
            ).get(BaseViewModel::class.java)
        }

        viewModel.listen(ListenTo.HomeFragmentStart)

        setObservations()
        setRecycler()
        setOnClicks()
    }

    private fun setOnClicks() {
        MoreEvents.setOnClickListener {
            findNavController().navigate(R.id.action_destination_home_to_destination_calender)
        }
        CoursesLink.setOnClickListener {
            findNavController().navigate(R.id.action_destination_home_to_destination_courses)
        }
        NotesLink.setOnClickListener {
            val directions = HomeFragmentDirections.actionDestinationHomeToDestinationNotes(HOME_COURSE_ID,
                HOME_COURSE_NAME)
            findNavController().navigate(directions)
        }
    }

    private fun setObservations() {
        activity?.let {
            viewModel.SelectableEvents.observe(
                it,
                Observer { event ->

                }
            )
        }
    }

    private fun setRecycler() {
        adapter = EventsRecyclerAdapter(events)
        adapter.eventClickListener.observe(
            this,
            Observer {
                findNavController().navigate(R.id.action_destination_home_to_destination_event_detail)
                viewModel.listen(it)
            }
        )
        UpcomingEventRec.adapter = adapter
        UpcomingEventRec.itemAnimator = DefaultItemAnimator()
        UpcomingEventRec.addItemDecoration(DividerItemDecoration(requireActivity(),DividerItemDecoration.VERTICAL))
        UpcomingEventRec.layoutManager = LinearLayoutManager(requireContext())
    }
}