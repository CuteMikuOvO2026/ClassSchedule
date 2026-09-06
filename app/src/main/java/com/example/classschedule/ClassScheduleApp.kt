package com.example.classschedule

import android.app.Application
import com.example.classschedule.di.AppContainer

class ClassScheduleApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
