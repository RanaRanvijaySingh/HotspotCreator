package com.simple.hotspotcreator.di

import com.simple.hotspotcreator.ViewModelModule
import com.simple.hotspotcreator.ui.HomeActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ViewModelModule::class
    ]
)
interface AppComponent {

    fun inject(activity: HomeActivity)
}