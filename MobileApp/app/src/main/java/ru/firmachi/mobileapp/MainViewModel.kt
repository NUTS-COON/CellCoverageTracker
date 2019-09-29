package ru.firmachi.mobileapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import ru.firmachi.mobileapp.models.CellData
import ru.firmachi.mobileapp.models.api.GetScoreRequest
import ru.firmachi.mobileapp.repositories.CellDataLocalRepository
import ru.firmachi.mobileapp.services.ApiService
import ru.firmachi.mobileapp.services.NetworkService
import javax.inject.Inject

class MainViewModel : ViewModel() {

    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var cellDataLocalRepository: CellDataLocalRepository

    val cellDataLiveData = MutableLiveData<List<CellData>>()
    val scoreLiveData = MutableLiveData<Int>()

    init {
        App.component.inject(this)
    }


    fun ready(networkService: NetworkService){
        networkService.retrieveCellData {
            cellDataLiveData.value = it
        }
        cellDataLocalRepository.setNotifyDataChanged {
            cellDataLiveData.value = it.sortedByDescending { c -> c.timestamp }.distinctBy { c -> c.imei }
        }

        GlobalScope.launch(Dispatchers.IO) {
            var score = 0

            networkService
                .getImei()
                .forEach {
                    val request = apiService.getScore(GetScoreRequest(it)).execute()
                    if(request.isSuccessful && request.body() != null){
                        score += request.body()!!
                    }
                }

            withContext(Dispatchers.Main){
                scoreLiveData.value = score
            }
        }
    }
}