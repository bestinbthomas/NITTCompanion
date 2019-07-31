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
import kotlinx.android.synthetic.main.fragment_course_detail.view.*


class CourseDetailFragment : Fragment() {

    private lateinit var viewModel: BaseViewModel
    private lateinit var course: Course
    private lateinit var adapter : EventsRecyclerAdapter
    private lateinit var mView : View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mView = view

        retainInstance = true
        activity?.let{viewModel = ViewModelProviders.of(requireActivity(), InjectorUtils(requireActivity().application).provideBaseViewModelFactory()).get(
            BaseViewModel::class.java)}

        course = viewModel.DispCourse.value!!
        viewModel.listen(ListenTo.CourseDetailStart)
        setUpRecycler()
        setObservations()
        setViews()
        setOnClicks()
    }

    private fun setUpRecycler()  =activity?.let{
        adapter = EventsRecyclerAdapter(listOf())
        mView.CourseAlertsRecView.adapter = adapter
        mView.CourseAlertsRecView.itemAnimator = DefaultItemAnimator()
        mView.CourseAlertsRecView.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))
        mView.CourseAlertsRecView.layoutManager = LinearLayoutManager(requireActivity())
        adapter.eventClickListener.observe(
            this,
            Observer {
                viewModel.listen(it)
                findNavController().navigate(R.id.action_destination_course_detail_to_destination_event_detail)
            }
        )
    }

    private fun setOnClicks()  =activity?.let{
        mView.AddEventBtn.setOnClickListener {
            viewModel.listen(ListenTo.AddEventForCourse)
            val directions = CourseDetailFragmentDirections.actionDestinationCourseDetailToDestinationEventCreate(course.ID)
            findNavController().navigate(directions)
        }
        mView.NotesBtn.setOnClickListener {
            val directions = CourseDetailFragmentDirections.actionDestinationCourseDetailToDestinationNotes(course.ID,course.name)
            findNavController().navigate(directions)
        }
        mView.DeleteCourseBtn.setOnClickListener {
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

        mView.EditCourseBtn.setOnClickListener {
            findNavController().navigate(R.id.action_destination_course_detail_to_destination_add_courses)
        }
        mView.attendedPlus.setOnClickListener {
            course.attended++
            course.calculateClasses()
            setViews()
            viewModel.listen(ListenTo.UpdateCourse(course))
        }
        mView.attendedMinus.setOnClickListener {
            course.attended--
            course.calculateClasses()
            setViews()
            viewModel.listen(ListenTo.UpdateCourse(course))
        }
        mView.bunkedPlus.setOnClickListener {
            course.notAttended++
            course.calculateClasses()
            setViews()
            viewModel.listen(ListenTo.UpdateCourse(course))
        }
        mView.bunkedMinus.setOnClickListener {
            course.notAttended--
            course.calculateClasses()
            setViews()
            viewModel.listen(ListenTo.UpdateCourse(course))
        }
    }

    private fun setViews() =activity?.let {
        mView.CourseName.text = course.name
        mView.Credits.text = requireActivity().resources.getString(R.string.credits_disp,course.credit)
        mView.EventAttendance.text = requireActivity().resources.getString(R.string.attendenceWithPercent,course.attendance.toInt())
        val classtoattend :Int = course.classToAttend
        mView.AttendenceStatus.text = if(classtoattend<=0) requireActivity().resources.getString(R.string.courseStatusBunk,(classtoattend*-1)) else requireActivity().resources.getString(R.string.courseStatusAttend,classtoattend)
        mView.Attended.text = requireActivity().resources.getString(R.string.attended,course.attended)
        mView.Bunked.text = requireActivity().resources.getString(R.string.bunked,course.notAttended)
        mView.AlertsCard.visibility = View.INVISIBLE
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
                    if(events.isNotEmpty())
                        mView.AlertsCard.visibility = View.VISIBLE
                    adapter.updateEvents(events)
                    setViews()
                }
            )
        }
    }
}
