package com.example.nittcompanion.common.factoryAndInjector

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.nittcompanion.model.repository.IRepo
import com.example.nittcompanion.model.repository.implementations.RepoImplementation
import com.google.firebase.FirebaseApp

class InjectorUtils(application: Application) : AndroidViewModel(application){

    private fun getEventRepo() : IRepo = RepoImplementation.getInstance()


    fun provideBaseViewModelFactory() : BaseViewModelFactory {
        FirebaseApp.initializeApp(getApplication())
        return BaseViewModelFactory(
            getEventRepo()
        )
    }
}