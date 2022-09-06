package com.simple.hotspotcreator

import android.app.Application
import android.content.Context
import com.simple.hotspotcreator.di.AppComponent
import com.simple.hotspotcreator.di.AppModule
import com.simple.hotspotcreator.di.DaggerAppComponent

class MyApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    companion object {
        operator fun get(context: Context): MyApplication {
            return context.applicationContext as MyApplication
        }
    }
}