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
import com.example.nittcompanion.common.factoryAndInjector.InjectorUtils
import com.example.nittcompanion.common.objects.Event
import com.example.nittcompanion.event.EventsRecyclerAdapter
import kotlinx.android.synthetic.main.fragment_calender.view.*
import java.util.*

class CalenderFragment : Fragment() {
    private lateinit var viewModel: BaseViewModel
    private lateinit var RecAdapter: EventsRecyclerAdapter
    private lateinit var GridAdapter: CalGridAdapter
    private lateinit var MonthArray: Array<String>
    private lateinit var selDate: Calendar
    private lateinit var mView: View
    private var monthEvents: List<Event> = listOf()
    private val TAG = "CalenderFragment"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        return inflater.inflate(R.layout.fragment_calender, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mView = view
        retainInstance = true
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

    private fun setUpCalender() = activity?.let {
        val events = viewModel.MonthlyEvents.value
        MonthArray = resources.getStringArray(R.array.Months)
        selDate = Calendar.getInstance()
        GridAdapter = CalGridAdapter(
            context = requireContext(),
            date = selDate,
            events = events ?: listOf()
        )
        mView.CalGrid.adapter = GridAdapter
        mView.MonthNameTxt.text = requireActivity().resources.getString(
            R.string.Month_Year,
            MonthArray[selDate.get(Calendar.MONTH)],
            selDate.get(Calendar.YEAR)
        )
    }

    private fun setObservations() = activity?.let {

        activity?.let {
            viewModel.DispDate.observe(it,
                Observer { cal ->
                    GridAdapter.updateDate(cal)
                    GridAdapter.notifyDataSetChanged()
                    selDate = cal
                    mView.MonthNameTxt.text = it.resources.getString(
                        R.string.Month_Year,
                        MonthArray[selDate.get(Calendar.MONTH)],
                        selDate.get(Calendar.YEAR)
                    )
                    Log.d(TAG, "Month Changed to ${cal.get(Calendar.MONTH)}")
                })
            viewModel.MonthlyEvents.observe(
                it,
                Observer { events ->
                    monthEvents = events
                    GridAdapter.updateEvents(monthEvents)
                    GridAdapter.notifyDataSetChanged()
                }
            )
            viewModel.SelectableEvents.observe(
                it,
                Observer { events ->
                    Log.d("CalenderFragment", "events changed")
                    RecAdapter.updateEvents(events)
                }
            )
        }

    }

    private fun setRecyclerView() = activity?.let {
        val events = viewModel.SelectableEvents.value
        RecAdapter = EventsRecyclerAdapter(events?: listOf()  )
        RecAdapter.eventClickListener.observe(
            this,
            Observer {
                findNavController().navigate(R.id.action_destination_calender_to_destination_event_detail)
                viewModel.listen(it)
            }
        )
        mView.EventRecycler.adapter = RecAdapter
        mView.EventRecycler.itemAnimator = DefaultItemAnimator()
        mView.EventRecycler.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))
        mView.EventRecycler.layoutManager = LinearLayoutManager(requireActivity())
    }

    private fun setClickListeners() = activity?.let {
        mView.CalGrid.setOnItemClickListener { _, _, _, id ->
            viewModel.listen(ListenTo.DateSelected(id.toInt()))
            Log.d(TAG, "date clicked $id")
        }

        mView.PrevMonthBtn.setOnClickListener {
            viewModel.listen(ListenTo.PreviousMonthClicked)
        }

        mView.NextMonthBtn.setOnClickListener {
            viewModel.listen(ListenTo.NextMonthClicked)
        }

        mView.AddEventFAB.setOnClickListener {
            viewModel.listen(ListenTo.AddNewEvent)
            findNavController().navigate(R.id.action_destination_calender_to_destination_event_create)
        }
    }


}