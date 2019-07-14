package com.example.nittcompanion.common.factoryAndInjector

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.nittcompanion.model.repository.IEventsRepo
import com.example.nittcompanion.model.repository.implementations.EventRepoImplementation
import com.google.firebase.FirebaseApp

class InjectorUtils(application: Application) : AndroidViewModel(application){

    private fun getEventRepo() : IEventsRepo = EventRepoImplementation()


    fun provideBaseViewModelFactory() : BaseViewModelFactory {
        FirebaseApp.initializeApp(getApplication())
        return BaseViewModelFactory(
            getEventRepo()
        )
    }
}