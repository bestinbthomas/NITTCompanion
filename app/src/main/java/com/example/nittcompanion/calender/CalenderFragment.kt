package com.example.nittcompanion.calender

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.nittcompanion.R
import com.example.nittcompanion.common.BaseViewModel
import com.example.nittcompanion.common.ListenTo
import com.example.nittcompanion.common.createSnackbar
import com.example.nittcompanion.common.factoryAndInjector.InjectorUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_calender.*
import java.time.Year
import java.util.*

class CalenderFragment : Fragment() {
    private lateinit var viewModel : BaseViewModel
    private lateinit var RecAdapter: CalenderEventsRecyclerAdapter
    private lateinit var GridAdapter: CalGridAdapter
    private lateinit var MonthArray: Array<String>
    private val TAG = "CalenderFragment"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {



        return inflater.inflate(R.layout.fragment_calender,container,false)

    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            viewModel = ViewModelProviders.of(it,InjectorUtils(it.application).provideBaseViewModelFactory()).get(BaseViewModel::class.java)
        }

        viewModel.listen(ListenTo.ScheduleFragmentstart)

        MonthArray = resources.getStringArray(R.array.Months)

        val cal = Calendar.getInstance()
        GridAdapter = CalGridAdapter(requireContext(),cal)
        CalGrid.adapter = GridAdapter
        MonthNameTxt.text = "${MonthArray[cal.get(Calendar.MONTH)]}  ${cal.get(Calendar.YEAR)}"

        setObservations()
        setClickListeners()
        setRecyclerView()

    }

    private fun setObservations() {

        activity?.let {
            viewModel.error.observe(
                it,
                androidx.lifecycle.Observer {msg ->
                    createSnackbar(msg,Snackbar.LENGTH_SHORT)
                }
            )
            viewModel.DispDate.observe(it,
                androidx.lifecycle.Observer {cal ->
                    GridAdapter = CalGridAdapter(requireContext(),cal)
                    CalGrid.adapter = GridAdapter
                    MonthNameTxt.text = "${MonthArray[cal.get(Calendar.MONTH)]}  ${cal.get(Calendar.YEAR)}"
                    Log.d(TAG,"Month Changed to ${cal.get(Calendar.MONTH)}")
                })
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
    }

    private fun setClickListeners(){
        CalGrid.setOnItemClickListener{ _, _, _, id ->
            viewModel.listen(ListenTo.DateSelected(id.toInt()))
            Log.d(TAG,"date clicked $id")
        }

        PrevMonthBtn.setOnClickListener{
            viewModel.listen(ListenTo.PreviourMonthClicked)
        }

        NextMonthBtn.setOnClickListener{
            viewModel.listen(ListenTo.NextMonthClicked)
        }

        AddEventFAB.setOnClickListener {
            findNavController().navigate(R.id.action_destination_calender_to_destination_event_create)
        }
    }


}