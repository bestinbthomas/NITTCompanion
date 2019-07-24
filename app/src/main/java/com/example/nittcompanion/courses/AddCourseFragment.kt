package com.example.nittcompanion.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.nittcompanion.R
import com.example.nittcompanion.common.BaseViewModel
import com.example.nittcompanion.common.ListenTo
import com.example.nittcompanion.common.createSnackbar
import com.example.nittcompanion.common.factoryAndInjector.InjectorUtils
import com.example.nittcompanion.common.objects.ClassEvent
import com.example.nittcompanion.common.objects.Course
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_add_course.view.*
import java.util.*

class AddCourseFragment : Fragment() {
    private lateinit var viewModel: BaseViewModel
    private lateinit var course: Course
    private var isLab = false
    private  lateinit var mView :View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_course, container, false)
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
            ).get(
                BaseViewModel::class.java
            )
        }

        course = viewModel.DispCourse.value!!
        isLab = course.classEvent.islab

        setObservations()
        setViews()
        setOnClicks()

    }

    private fun setOnClicks() =activity?.let {
        mView.saveCourse.setOnClickListener{
            saveCourse()
        }
        mView.IsLab.setOnCheckedChangeListener { _, isChecked ->
            isLab = isChecked
            setViews()
        }
    }

    private fun validateName(): Boolean = if (mView.CourseNameInputLayout.editText!!.text.toString().isBlank()) {
        mView.CourseNameInputLayout.error = "please enter a name"
        false
    } else {
        mView. CourseNameInputLayout.error = null
        true
    }


    private fun validateCredits(): Boolean = if (mView.CourseCreditsInputLayout.editText!!.text.toString().isBlank()) {
        mView.CourseCreditsInputLayout.error = "please enter credits"
        false
    } else {
        mView.CourseCreditsInputLayout.error = null
        true
    }

    private fun saveCourse()  {
        if (!validateName() or !validateCredits())
            return
        else {

            val classEvent = ClassEvent(
                hashMapOf(
                    Calendar.MONDAY.toString() to mView.MonSlotPicker.selectedItemPosition,
                    Calendar.TUESDAY.toString() to mView.TueSlotPicker.selectedItemPosition,
                    Calendar.WEDNESDAY.toString() to mView.WedSlotPicker.selectedItemPosition,
                    Calendar.THURSDAY.toString() to mView.ThuSlotPicker.selectedItemPosition,
                    Calendar.FRIDAY.toString() to mView.FriSlotPicker.selectedItemPosition
                ), isLab
            )
            viewModel.listen(
                ListenTo.UpdateCourse(
                    Course(
                        mView.CourseNameInputLayout.editText!!.text.toString(),
                        mView.CourseCreditsInputLayout.editText!!.text.toString().toInt(),
                        classEvent
                    ),
                    false
                )
            )
            createSnackbar("Course Created", Snackbar.LENGTH_SHORT)
            findNavController().popBackStack()
        }
    }

    private fun setViews()  =activity?.let{
        mView.IsLab.isChecked = isLab
        val slots = if (!isLab)
            requireActivity().resources.getStringArray(R.array.slots)
        else {
            mView.MonSlotPicker.setSelection(0)
            mView.TueSlotPicker.setSelection(0)
            mView.WedSlotPicker.setSelection(0)
            mView.ThuSlotPicker.setSelection(0)
            mView.FriSlotPicker.setSelection(0)
            arrayOf("None","Morning","Evening")
        }
        val slotAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_dropdown_item,slots)
        mView.MonSlotPicker.adapter = slotAdapter
        mView.TueSlotPicker.adapter = slotAdapter
        mView.WedSlotPicker.adapter = slotAdapter
        mView.ThuSlotPicker.adapter = slotAdapter
        mView.FriSlotPicker.adapter = slotAdapter
        course.let {
            mView.CourseNameInputLayout.editText!!.setText(it.name, TextView.BufferType.EDITABLE)
            if(it.credit!=0)
                mView.CourseCreditsInputLayout.editText!!.setText(it.credit.toString(), TextView.BufferType.EDITABLE)

            mView.MonSlotPicker.setSelection(it.classEvent.classes[Calendar.MONDAY.toString()] ?: 0)
            mView.TueSlotPicker.setSelection(it.classEvent.classes[Calendar.TUESDAY.toString()] ?: 0)
            mView.WedSlotPicker.setSelection(it.classEvent.classes[Calendar.WEDNESDAY.toString()] ?: 0)
            mView.ThuSlotPicker.setSelection(it.classEvent.classes[Calendar.THURSDAY.toString()] ?: 0)
            mView.FriSlotPicker.setSelection(it.classEvent.classes[Calendar.FRIDAY.toString()] ?: 0)
        }
    }

    private fun setObservations() =activity?.let{
        activity?.let { act ->
            viewModel.DispCourse.observe(
                act,
                Observer {
                    course = it
                    isLab = course.classEvent.islab
                    setViews()
                }
            )
        }
    }


}