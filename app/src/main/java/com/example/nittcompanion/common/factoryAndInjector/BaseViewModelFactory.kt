package com.example.nittcompanion.common.factoryAndInjector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nittcompanion.common.BaseViewModel
import com.example.nittcompanion.model.repository.IRepo
import kotlinx.coroutines.Dispatchers

class BaseViewModelFactory(
    private val eventRepo: IRepo
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BaseViewModel(uicontext = Dispatchers.Main,repo = eventRepo) as T
    }
}