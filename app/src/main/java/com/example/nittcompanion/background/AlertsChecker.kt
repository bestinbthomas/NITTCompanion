package com.example.nittcompanion.background

import android.app.job.JobParameters
import android.app.job.JobService

class AlertsChecker : JobService() {
    private var JobCancelled = false
    override fun onStopJob(params: JobParameters?): Boolean {
        Thread(Runnable {

        }).start()
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        JobCancelled = true
        return true
    }
}