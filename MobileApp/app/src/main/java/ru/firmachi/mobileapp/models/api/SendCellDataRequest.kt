package ru.firmachi.mobileapp.models.api


data class SendCellDataRequest(
    val latitude: Double,
    val longitude: Double,
    val cellType: String,
    val operatorName: String,
    val level: Int,
    val timestamp: String,
    val imei: String)