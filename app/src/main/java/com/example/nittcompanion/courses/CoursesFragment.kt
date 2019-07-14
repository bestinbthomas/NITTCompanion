package com.example.nittcompanion.courses

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
import com.example.nittcompanion.common.factoryAndInjector.InjectorUtils
import kotlinx.android.synthetic.main.fragment_calender.*
import kotlinx.android.synthetic.main.fragment_courses.*

class CoursesFragment : Fragment() {
    private lateinit var viewModel: BaseViewModel
    private lateinit var adapter : CourcesRecyclerAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_courses,container,false)
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            viewModel = ViewModelProviders.of(it, InjectorUtils(it.application).provideBaseViewModelFactory()).get(
            BaseViewModel::class.java)
        }
        setUpRecycler()
        setObservations()
        setOnClicks( )
    }

    private fun setOnClicks() {
        AddEventFAB.setOnClickListener {
            findNavController().navigate(R.id.action_destination_courses_to_destination_add_courses)
        }
    }

    private fun setUpRecycler() {
        adapter = CourcesRecyclerAdapter(requireContext(), listOf())
        CoursesRecycler.adapter = adapter
        CoursesRecycler.itemAnimator = DefaultItemAnimator()
        CoursesRecycler.addItemDecoration(DividerItemDecoration(requireActivity(),DividerItemDecoration.VERTICAL))
        CoursesRecycler.layoutManager = LinearLayoutManager(requireActivity())
        adapter.eventSelectListen.observe(
            this,
            Observer {
                viewModel.listen(it)
                findNavController().navigate(R.id.action_destination_courses_to_destination_course_detail)
            }
        )
    }

    private fun setObservations() {
        activity?.let {activity ->
            viewModel.Courses.observe(
                activity,
                Observer {courses ->
                    adapter.updateCources(courses)
                }
            )
        }
    }
}