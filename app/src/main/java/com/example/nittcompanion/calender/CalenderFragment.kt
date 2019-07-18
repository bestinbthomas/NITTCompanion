package com.example.nittcompanion.calender

import android.os.Bundle
import android.util.Log
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
import com.example.nittcompanion.common.ListenTo
import com.example.nittcompanion.common.createSnackbar
import com.example.nittcompanion.common.factoryAndInjector.InjectorUtils
import com.example.nittcompanion.common.objects.Event
import com.example.nittcompanion.event.EventsRecyclerAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_calender.*
import java.util.*

class CalenderFragment : Fragment() {
    private lateinit var viewModel: BaseViewModel
    private lateinit var RecAdapter: EventsRecyclerAdapter
    private lateinit var GridAdapter: CalGridAdapter
    private lateinit var MonthArray: Array<String>
    private lateinit var selDate: Calendar
    private var monthEvents :List<Event> = listOf()
    private val TAG = "CalenderFragment"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        return inflater.inflate(R.layout.fragment_calender, container, false)

    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            viewModel = ViewModelProviders.of(it, InjectorUtils(it.application).provideBaseViewModelFactory())
                .get(BaseViewModel::class.java)
        }

        viewModel.listen(ListenTo.ScheduleFragmentstart)


        setUpCalender()
        setRecyclerView()
        setObservations()
        setClickListeners()


    }

    private fun setUpCalender() {
        val events = viewModel.MonthlyEvents.value
        val alerts = viewModel.alerts.value
        MonthArray = resources.getStringArray(R.array.Months)
        selDate = Calendar.getInstance()
        GridAdapter = CalGridAdapter(context = requireContext(),date =  selDate,events = events?: listOf(),alerts = alerts?: listOf())
        CalGrid.adapter = GridAdapter
        MonthNameTxt.text = requireActivity().resources.getString(R.string.Month_Year,MonthArray[selDate.get(Calendar.MONTH)],selDate.get(Calendar.YEAR))
    }

    private fun setObservations() {

        activity?.let {
            viewModel.error.observe(
                it,
                Observer { msg ->
                    createSnackbar(msg, Snackbar.LENGTH_SHORT)
                }
            )
            viewModel.DispDate.observe(it,
                Observer { cal ->
                    GridAdapter.updateDate(cal)
                    CalGrid.adapter = GridAdapter
                    MonthNameTxt.text = requireActivity().resources.getString(R.string.Month_Year,MonthArray[cal.get(Calendar.MONTH)],cal.get(Calendar.YEAR))
                    selDate = cal
                    Log.d(TAG, "Month Changed to ${cal.get(Calendar.MONTH)}")
                })
            viewModel.MonthlyEvents.observe(
                it,
                Observer { events ->
                    monthEvents = events
                    GridAdapter.updateEvents(monthEvents)
                    CalGrid.adapter = GridAdapter
                }
            )
            viewModel.SelectableEvents.observe(
                it,
                Observer {events ->
                    RecAdapter.updateEvents(events)
                }
            )
            viewModel.alerts.observe(
                it,
                Observer { alerts ->
                    GridAdapter.updateAlerts(alerts)
                    CalGrid.adapter = GridAdapter
                }
            )
        }

    }

    private fun setRecyclerView() {
        val events = viewModel.SelectableEvents.value
        RecAdapter = EventsRecyclerAdapter(events?:listOf())
        RecAdapter.eventClickListener.observe(
            this,
            androidx.lifecycle.Observer {
                viewModel.listen(it)
            }
        )
        EventRecycler.adapter=RecAdapter
        EventRecycler.itemAnimator = DefaultItemAnimator()
        EventRecycler.addItemDecoration(DividerItemDecoration(requireActivity(),DividerItemDecoration.VERTICAL))
        EventRecycler.layoutManager = LinearLayoutManager(requireActivity())
    }

    private fun setClickListeners() {
        CalGrid.setOnItemClickListener { _, _, _, id ->
            viewModel.listen(ListenTo.DateSelected(id.toInt()))
            Log.d(TAG, "date clicked $id")
        }

        PrevMonthBtn.setOnClickListener {
            viewModel.listen(ListenTo.PreviousMonthClicked)
        }

        NextMonthBtn.setOnClickListener {
            viewModel.listen(ListenTo.NextMonthClicked)
        }

        AddEventFAB.setOnClickListener {
            viewModel.listen(ListenTo.AddNewEvent)
            findNavController().navigate(R.id.action_destination_calender_to_destination_event_create)
        }
    }


}