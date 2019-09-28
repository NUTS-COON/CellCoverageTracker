package ru.firmachi.mobileapp.di

import dagger.Component
import ru.firmachi.mobileapp.repositories.CellDataLocalRepository
import ru.firmachi.mobileapp.services.TrackingService
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(trackingService: TrackingService)
    fun getCellDataLocalRepository(): CellDataLocalRepository
}