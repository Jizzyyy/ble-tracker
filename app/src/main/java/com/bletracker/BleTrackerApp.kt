package com.bletracker

import android.app.Application
import com.bletracker.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BleTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BleTrackerApp)
            modules(appModules)
        }
    }
}
