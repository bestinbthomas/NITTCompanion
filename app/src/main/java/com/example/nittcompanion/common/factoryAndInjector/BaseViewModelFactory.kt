package com.example.nittcompanion.common.factoryAndInjector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nittcompanion.common.BaseViewModel
import com.example.nittcompanion.model.repository.IEventsRepo
import kotlinx.coroutines.Dispatchers

class BaseViewModelFactory(
    private val eventRepo: IEventsRepo
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BaseViewModel(uicontext = Dispatchers.Main,eventRepo = eventRepo) as T
    }
}