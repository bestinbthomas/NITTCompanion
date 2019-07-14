package com.example.nittcompanion.courses


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.nittcompanion.R
import com.example.nittcompanion.common.BaseViewModel
import com.example.nittcompanion.common.factoryAndInjector.InjectorUtils
import com.example.nittcompanion.common.objects.Course


class CourseDetail : Fragment() {

    private lateinit var viewModel: BaseViewModel
    private lateinit var course: Course

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_detail, container, false)
    }

    override fun onStart() {
        super.onStart()

        activity?.let{viewModel = ViewModelProviders.of(requireActivity(), InjectorUtils(requireActivity().application).provideBaseViewModelFactory()).get(
            BaseViewModel::class.java)}

        course = viewModel.DispCourse.value!!
        setObservations()
        setViews()
        setOnClicks()
    }

    private fun setOnClicks() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun setViews() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun setObservations() {
        activity?.let { act ->
            viewModel.DispCourse.observe(
                act,
                Observer { DispCourse ->
                    course = DispCourse
                }
            )
        }
    }
}
