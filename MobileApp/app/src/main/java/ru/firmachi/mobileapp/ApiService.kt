package ru.firmachi.mobileapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import ru.firmachi.mobileapp.models.api.SendCellDataRequest
import ru.firmachi.mobileapp.models.api.SendCellDataResponse

interface ApiService {

    @POST("api/Data/SaveMany")
    fun sendCellData(@Body cellData: List<SendCellDataRequest>): Call<SendCellDataResponse>
}