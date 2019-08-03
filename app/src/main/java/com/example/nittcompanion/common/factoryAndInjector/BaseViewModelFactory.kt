package com.example.nittcompanion.common.factoryAndInjector

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nittcompanion.common.BaseViewModel
import com.example.nittcompanion.model.repository.IRepo
import kotlinx.coroutines.Dispatchers

class BaseViewModelFactory(
    private val application: Application,
    private val eventRepo: IRepo
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BaseViewModel(uicontext = Dispatchers.Main,repo = eventRepo,app = application) as T
    }
}