package com.bool.gymtracker

import android.app.Application
import com.bool.gymtracker.di.AppContainer

class GymTrackerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
