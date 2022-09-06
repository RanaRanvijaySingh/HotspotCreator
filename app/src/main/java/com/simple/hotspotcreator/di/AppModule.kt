package com.simple.hotspotcreator.di

import android.content.Context
import com.simple.hotspotcreator.MyApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val application: MyApplication) {

    @Provides
    @Singleton
    fun providesApplication(): MyApplication {
        return application
    }

    @Provides
    @Singleton
    fun providesContext(application: MyApplication): Context {
        return application.baseContext
    }
}
