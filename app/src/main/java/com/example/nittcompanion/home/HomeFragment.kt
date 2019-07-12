package com.example.nittcompanion.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.nittcompanion.R
import com.example.nittcompanion.common.BaseViewModel
import com.example.nittcompanion.common.factoryAndInjector.InjectorUtils

class HomeFragment : Fragment() {
    private lateinit var viewModel: BaseViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home,container,false);
    }

    override fun onStart() {
        super.onStart()
        viewModel = ViewModelProviders.of(requireActivity(), InjectorUtils(requireActivity().application).provideBaseViewModelFactory()).get(BaseViewModel::class.java)
    }
}