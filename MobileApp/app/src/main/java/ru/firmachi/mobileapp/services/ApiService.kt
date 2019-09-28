package ru.firmachi.mobileapp.services

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import ru.firmachi.mobileapp.models.api.GetScoreRequest
import ru.firmachi.mobileapp.models.api.SendCellDataRequest
import ru.firmachi.mobileapp.models.api.SendCellDataResponse

interface ApiService {

    @POST("api/Data/SaveMany")
    fun sendCellData(@Body cellData: List<SendCellDataRequest>): Call<SendCellDataResponse>

    @POST("api/Data/CountByImei")
    fun getScore(@Body model: GetScoreRequest): Call<Int>
}