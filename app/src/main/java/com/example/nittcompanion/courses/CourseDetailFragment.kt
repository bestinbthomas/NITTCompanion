package com.example.nittcompanion.courses


import android.app.AlertDialog
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
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.event.EventsRecyclerAdapter
import kotlinx.android.synthetic.main.fragment_course_detail.*


class CourseDetailFragment : Fragment() {

    private lateinit var viewModel: BaseViewModel
    private lateinit var course: Course
    private lateinit var adapter : EventsRecyclerAdapter

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
        viewModel.listen(ListenTo.CourseDetailStart)
        setUpRecycler()
        setObservations()
        setViews()
        setOnClicks()
    }

    private fun setUpRecycler() {
        adapter = EventsRecyclerAdapter(listOf())
        CourseAlertsRecView.adapter = adapter
        CourseAlertsRecView.itemAnimator = DefaultItemAnimator()
        CourseAlertsRecView.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))
        CourseAlertsRecView.layoutManager = LinearLayoutManager(requireActivity())
        adapter.eventClickListener.observe(
            this,
            Observer {
                viewModel.listen(it)
                findNavController().navigate(R.id.action_destination_course_detail_to_destination_event_detail)
            }
        )
    }

    private fun setOnClicks() {
        AddEventBtn.setOnClickListener {
            viewModel.listen(ListenTo.AddEventForCourse)
            val directions = CourseDetailFragmentDirections.actionDestinationCourseDetailToDestinationEventCreate(course.ID)
            findNavController().navigate(directions)
        }
        NotesBtn.setOnClickListener {
            val directions = CourseDetailFragmentDirections.actionDestinationCourseDetailToDestinationNotes(course.ID,course.name)
            findNavController().navigate(directions)
        }
        DeleteCourseBtn.setOnClickListener {
            val alert :AlertDialog = AlertDialog.Builder(requireContext())
                .setTitle("Confirm Delete")
                .setMessage("Do you really want to delete the course ")
                .setPositiveButton("Yes, Delete "){_, _ ->
                    viewModel.listen(ListenTo.RemoveCourse)
                    findNavController().popBackStack()
                }
                .setNegativeButton("Cancel"){_, _ ->

                }
                .create()
            alert.show()
        }

        EditCourseBtn.setOnClickListener {
            findNavController().navigate(R.id.action_destination_course_detail_to_destination_add_courses)
        }
        attendedPlus.setOnClickListener {
            course.attended++
            course.calculateClasses()
            viewModel.listen(ListenTo.UpdateCourse(course,false))
        }
        attendedMinus.setOnClickListener {
            course.attended--
            course.calculateClasses()
            viewModel.listen(ListenTo.UpdateCourse(course,false))
        }
        bunkedPlus.setOnClickListener {
            course.notAttended++
            course.calculateClasses()
            viewModel.listen(ListenTo.UpdateCourse(course,false))
        }
        bunkedPlus.setOnClickListener {
            course.notAttended--
            course.calculateClasses()
            viewModel.listen(ListenTo.UpdateCourse(course,false))
        }
    }

    private fun setViews() {
        CourseName.text = course.name
        Credits.text = requireActivity().resources.getString(R.string.credits_disp,course.credit)
        EventAttendance.text = requireActivity().resources.getString(R.string.attendenceWithPercent,course.attendance)
        val classtoattend :Int = course.classToAttend.value!!
        AttendenceStatus.text = if(classtoattend<0) requireActivity().resources.getString(R.string.courseStatusBunk,(classtoattend*-1)) else requireActivity().resources.getString(R.string.courseStatusAttend,classtoattend)
        Attended.text = requireActivity().resources.getString(R.string.attended,course.attended)
        Bunked.text = requireActivity().resources.getString(R.string.bunked,course.notAttended)
    }

    private fun setObservations() {
        activity?.let { act ->
            viewModel.DispCourse.observe(
                act,
                Observer { DispCourse ->
                    course = DispCourse
                    setViews()
                }
            )
            viewModel.SelectableEvents.observe(
                act,
                Observer { events ->
                    adapter.updateEvents(events)
                    setViews()
                }
            )
        }
    }
}
