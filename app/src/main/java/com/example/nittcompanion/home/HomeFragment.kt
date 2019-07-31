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
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {
    private lateinit var viewModel: BaseViewModel
    private var adapter: EventsRecyclerAdapter = EventsRecyclerAdapter(listOf())
    private var events = listOf<Event>()
    private lateinit var mView :View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        retainInstance = true

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

    private fun setOnClicks()  =activity?.let{
        mView.MoreEvents.setOnClickListener {
            findNavController().navigate(R.id.action_destination_home_to_destination_calender)
        }
        mView.CoursesCard.setOnClickListener {
            findNavController().navigate(R.id.action_destination_home_to_destination_courses)
        }
        mView.NotesLinkCard.setOnClickListener {
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
                    adapter.updateEvents(event)
                    activity?.let {
                        if (event.isNotEmpty()){
                            mView.MoreEvents.text = resources.getString(R.string.more_events)
                        }else{
                            mView.MoreEvents.text = resources.getString(R.string.see_events)
                        }
                    }

                }
            )
        }
    }

    private fun setRecycler()  =activity?.let{
        adapter = EventsRecyclerAdapter(events)
        adapter.eventClickListener.observe(
            this,
            Observer {
                findNavController().navigate(R.id.action_destination_home_to_destination_event_detail)
                viewModel.listen(it)
            }
        )
        mView.UpcomingEventRec.adapter = adapter
        mView.UpcomingEventRec.itemAnimator = DefaultItemAnimator()
        mView.UpcomingEventRec.addItemDecoration(DividerItemDecoration(requireActivity(),DividerItemDecoration.VERTICAL))
        mView.UpcomingEventRec.layoutManager = LinearLayoutManager(requireContext())
    }
}