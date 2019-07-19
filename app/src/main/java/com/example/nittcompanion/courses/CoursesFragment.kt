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
import com.example.nittcompanion.common.ListenTo
import com.example.nittcompanion.common.factoryAndInjector.InjectorUtils
import kotlinx.android.synthetic.main.fragment_courses.view.*

class CoursesFragment : Fragment() {
    private lateinit var viewModel: BaseViewModel
    private lateinit var adapter : CourcesRecyclerAdapter
    private lateinit var mView : View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_courses,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        retainInstance = true
        activity?.let {
            viewModel = ViewModelProviders.of(it, InjectorUtils(it.application).provideBaseViewModelFactory()).get(
            BaseViewModel::class.java)
        }
        setUpRecycler()
        setObservations()
        setOnClicks( )
    }

    private fun setOnClicks() =activity?.let {
        mView.addCourseFAB.setOnClickListener {
            viewModel.listen(ListenTo.AddNewCourse)
            findNavController().navigate(R.id.action_destination_courses_to_destination_add_courses)
        }
    }

    private fun setUpRecycler() =activity?.let {
        adapter = CourcesRecyclerAdapter(requireContext(), listOf())
        mView.CoursesRecycler.adapter = adapter
        mView.CoursesRecycler.itemAnimator = DefaultItemAnimator()
        mView.CoursesRecycler.addItemDecoration(DividerItemDecoration(requireActivity(),DividerItemDecoration.VERTICAL))
        mView.CoursesRecycler.layoutManager = LinearLayoutManager(requireActivity())
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