package ru.firmachi.mobileapp

import android.app.Application
import io.realm.Realm
import ru.firmachi.mobileapp.di.ApplicationComponent
import ru.firmachi.mobileapp.di.ApplicationModule
import ru.firmachi.mobileapp.di.DaggerApplicationComponent

class App : Application() {

    companion object{
        lateinit var component: ApplicationComponent
    }


    override fun onCreate() {
        super.onCreate()
        initRealm()
        initDagger()
    }


    private fun initRealm(){
        Realm.init(this)
    }


    private fun initDagger(){
        component = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule())
            .build()
    }
}