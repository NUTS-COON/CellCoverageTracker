package ru.firmachi.mobileapp.di

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.firmachi.mobileapp.services.ApiService
import ru.firmachi.mobileapp.repositories.CellDataLocalRepository
import ru.firmachi.mobileapp.repositories.CellDataLocalRepositoryImpl
import javax.inject.Singleton

@Module
class ApplicationModule {

    @Provides
    @Singleton
    fun getApiService(): ApiService {
        return getRetrofit("https://cellcoverage.azurewebsites.net/").create(ApiService::class.java)
    }


    @Provides
    @Singleton
    fun getCellDataLocalRepository(): CellDataLocalRepository{
        return CellDataLocalRepositoryImpl()
    }

    private fun getRetrofit(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}