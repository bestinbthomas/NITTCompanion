package com.example.nittcompanion.common.factoryAndInjector

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.nittcompanion.model.repository.IRepo
import com.example.nittcompanion.model.repository.implementations.RepoImplementation

class InjectorUtils(application: Application) : AndroidViewModel(application){

    private fun getEventRepo() : IRepo = RepoImplementation.getInstance()


    fun provideBaseViewModelFactory() : BaseViewModelFactory {
        return BaseViewModelFactory(
            getEventRepo()
        )
    }
}