package com.example.nittcompanion.calender

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_calender.*
import java.util.*

class CalenderFragment : Fragment() {
    private lateinit var viewModel: BaseViewModel
    private lateinit var RecAdapter: CalenderEventsRecyclerAdapter
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

        MonthArray = resources.getStringArray(R.array.Months)

        selDate = Calendar.getInstance()
        GridAdapter = CalGridAdapter(requireContext(), selDate)
        CalGrid.adapter = GridAdapter
        MonthNameTxt.text = requireActivity().resources.getString(R.string.Month_Year,MonthArray[selDate.get(Calendar.MONTH)],selDate.get(Calendar.YEAR))

        setRecyclerView()
        setObservations()
        setClickListeners()


    }

    private fun setObservations() {

        activity?.let {
            viewModel.error.observe(
                it,
                androidx.lifecycle.Observer { msg ->
                    createSnackbar(msg, Snackbar.LENGTH_SHORT)
                }
            )
            viewModel.DispDate.observe(it,
                androidx.lifecycle.Observer { cal ->
                    GridAdapter = CalGridAdapter(requireContext(), cal,monthEvents)
                    CalGrid.adapter = GridAdapter
                    MonthNameTxt.text = requireActivity().resources.getString(R.string.Month_Year,MonthArray[cal.get(Calendar.MONTH)],cal.get(Calendar.YEAR))
                    selDate = cal
                    Log.d(TAG, "Month Changed to ${cal.get(Calendar.MONTH)}")
                })
            viewModel.MonthlyEvents.observe(
                it,
                androidx.lifecycle.Observer { events ->
                    monthEvents = events
                    GridAdapter = CalGridAdapter(requireContext(),selDate,monthEvents)
                    CalGrid.adapter = GridAdapter
                }
            )
            viewModel.SelectableEvents.observe(
                it,
                androidx.lifecycle.Observer {events ->
                    RecAdapter.updateEvents(events)
                }
            )
        }

    }

    private fun setRecyclerView() {
        RecAdapter = CalenderEventsRecyclerAdapter(listOf())
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
            viewModel.listen(ListenTo.PreviourMonthClicked)
        }

        NextMonthBtn.setOnClickListener {
            viewModel.listen(ListenTo.NextMonthClicked)
        }

        AddEventFAB.setOnClickListener {
            findNavController().navigate(R.id.action_destination_calender_to_destination_event_create)
        }
    }


}