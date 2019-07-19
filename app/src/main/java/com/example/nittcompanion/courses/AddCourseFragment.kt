package com.example.nittcompanion.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.fragment_add_course.*
import java.util.*

class AddCourseFragment : Fragment() {
    private lateinit var viewModel: BaseViewModel
    private lateinit var course: Course
    private var isLab = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_course, container, false)
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

        setObservations()
        setViews()
        setOnClicks()
    }

    private fun setOnClicks() {
        saveCourse.setOnClickListener{
            saveCourse()
        }
        IsLab.setOnCheckedChangeListener { _, isChecked ->
            isLab = isChecked
            setViews()
        }
    }

    private fun validateName(): Boolean = if (CourseNameInputLayout.editText!!.text.toString().isEmpty()) {
        CourseNameInputLayout.error = "please enter a name"
        true
    } else {
        CourseNameInputLayout.error = null
        false
    }


    private fun validateCredits(): Boolean = if (CourseCreditsInputLayout.editText!!.text.toString().isEmpty()) {
        CourseNameInputLayout.error = "please enter a name"
        true
    } else {
        CourseNameInputLayout.error = null
        false
    }

    private fun saveCourse() {
        if (!validateName() or !validateCredits()) return

        val classEvent = ClassEvent(
            hashMapOf(
                Calendar.MONDAY to MonSlotPicker.value,
                Calendar.TUESDAY to TueSlotPicker.value,
                Calendar.WEDNESDAY to WedSlotPicker.value,
                Calendar.THURSDAY to ThuSlotPicker.value,
                Calendar.FRIDAY to FriSlotPicker.value
            ), isLab
        )
        viewModel.listen(
            ListenTo.UpdateCourse(
                Course(
                    CourseNameInputLayout.editText!!.text.toString(),
                    CourseCreditsInputLayout.editText!!.text.toString().toInt(),
                    classEvent
                ),
                false
            )
        )
        createSnackbar("Course Created",Snackbar.LENGTH_SHORT)
        findNavController().popBackStack()
    }

    private fun setViews() {
        val slots = if (!isLab)
            requireActivity().resources.getStringArray(R.array.slots)
        else
            arrayOf("None","Morning","Evening")
        MonSlotPicker.minValue = 0
        MonSlotPicker.maxValue = slots.size - 1
        MonSlotPicker.wrapSelectorWheel = false
        MonSlotPicker.displayedValues = slots
        TueSlotPicker.minValue = 0
        TueSlotPicker.maxValue = slots.size - 1
        TueSlotPicker.wrapSelectorWheel = false
        TueSlotPicker.displayedValues = slots
        WedSlotPicker.minValue = 0
        WedSlotPicker.maxValue = slots.size - 1
        WedSlotPicker.wrapSelectorWheel = false
        WedSlotPicker.displayedValues = slots
        ThuSlotPicker.minValue = 0
        ThuSlotPicker.maxValue = slots.size - 1
        ThuSlotPicker.wrapSelectorWheel = false
        ThuSlotPicker.displayedValues = slots
        FriSlotPicker.minValue = 0
        FriSlotPicker.maxValue = slots.size - 1
        FriSlotPicker.wrapSelectorWheel = false
        FriSlotPicker.displayedValues = slots
        course.let {
            CourseNameInputLayout.editText!!.setText(it.name, TextView.BufferType.EDITABLE)
            CourseCreditsInputLayout.editText!!.setText(it.credit, TextView.BufferType.EDITABLE)

            MonSlotPicker.value = it.classEvent.classes[Calendar.MONDAY] ?: 0
            TueSlotPicker.value = it.classEvent.classes[Calendar.TUESDAY] ?: 0
            WedSlotPicker.value = it.classEvent.classes[Calendar.WEDNESDAY] ?: 0
            ThuSlotPicker.value = it.classEvent.classes[Calendar.THURSDAY] ?: 0
            FriSlotPicker.value = it.classEvent.classes[Calendar.FRIDAY] ?: 0
        }
    }

    private fun setObservations() {
        activity?.let { act ->
            viewModel.DispCourse.observe(
                act,
                Observer {
                    course = it
                    setViews()
                }
            )
        }
    }


}