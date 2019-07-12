package com.example.nittcompanion.Event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.nittcompanion.R
import com.example.nittcompanion.common.BaseViewModel
import com.example.nittcompanion.common.factoryAndInjector.InjectorUtils

class EventDetailFragment : Fragment() {
    private lateinit var viewModel: BaseViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_detail,container,false)
    }

    override fun onStart() {
        super.onStart()
        viewModel = ViewModelProviders.of(requireActivity(), InjectorUtils(requireActivity().application).provideBaseViewModelFactory()).get(BaseViewModel::class.java)

        setOnClicks()
    }

    private fun setOnClicks() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}