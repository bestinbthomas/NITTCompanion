package com.example.nittcompanion.common.factoryAndInjector

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.nittcompanion.model.repository.IRepo
import com.example.nittcompanion.model.repository.implementations.RepoImplementation

class InjectorUtils(val app: Application) : AndroidViewModel(app){

    private fun getEventRepo() : IRepo = RepoImplementation.getInstance(app)


    fun provideBaseViewModelFactory() : BaseViewModelFactory {
        return BaseViewModelFactory(
            getEventRepo()
        )
    }
}