package ru.firmachi.mobileapp.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.telephony.*
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import ru.firmachi.mobileapp.models.CellData
import java.text.SimpleDateFormat
import java.util.*


class NetworkService(
    private val context: Context,
    private val telephonyManager: TelephonyManager,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val subscriptionManager: SubscriptionManager) {


    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK)
    private lateinit var locationCallback: LocationCallback


    fun retrieveCellData(onSuccess: (List<CellData>) -> Unit) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                fusedLocationClient.removeLocationUpdates(locationCallback)
                if (locationResult != null && locationResult.locations.isNotEmpty()) {
                    onSuccess(getCellData(locationResult.locations[0]))
                    Log.d("TRACK", "onLocationResultSuccess")
                }else{
                    onSuccess(emptyList())
                    Log.d("TRACK", "onLocationResultBad")
                }
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
        fusedLocationClient.requestLocationUpdates(LocationRequest(), locationCallback, null)
    }

    fun getImei(): List<String>{
        val activeSimCount = telephonyManager.allCellInfo.filter { c -> c.isRegistered }.size
        return (0 until activeSimCount).map {
            telephonyManager.getDeviceId(it)
        }
    }


    private fun getCellData(location: Location?): MutableList<CellData> {
        val cellDataToSave = mutableListOf<CellData>()
        val operatorsName = getOperatorsName()

        if(location != null && permissionsGranted()){
            val activeSim = telephonyManager.allCellInfo.filter { c -> c.isRegistered }
            for(i in activeSim.indices){
                val cellData = CellData()
                when(val cellInfo = activeSim[i]) {
                    is CellInfoGsm -> {
                        cellData.dbm = cellInfo.cellSignalStrength.dbm
                        cellData.level = cellInfo.cellSignalStrength.level
                        cellData.cellType = "2G"
                    }
                    is CellInfoCdma -> {
                        cellData.dbm = cellInfo.cellSignalStrength.dbm
                        cellData.level = cellInfo.cellSignalStrength.level
                        cellData.cellType = "3G"
                    }
                    is CellInfoWcdma -> {
                        cellData.dbm = cellInfo.cellSignalStrength.dbm
                        cellData.level = cellInfo.cellSignalStrength.level
                        cellData.cellType = "3G"
                    }
                    is CellInfoLte -> {
                        cellData.dbm = cellInfo.cellSignalStrength.dbm
                        cellData.level = cellInfo.cellSignalStrength.level
                        cellData.cellType = "4G"
                    }
                    else -> {
                        cellData.dbm = 0
                        cellData.level = 0
                        cellData.cellType = "2G"
                    }
                }

                cellData.latitude = location.latitude
                cellData.longitude = location.longitude
                cellData.operatorName = operatorsName[i]
                cellData.timestamp = getTimestamp()
                cellData.imei = telephonyManager.getDeviceId(i)

                cellDataToSave.add(cellData)
            }
        }

        return cellDataToSave
    }


    private fun permissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }


    private fun getOperatorsName(): Array<String>{
        val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
        val res = arrayOfNulls<String>(activeSubscriptionInfoList.size)

        for (subscriptionInfo in activeSubscriptionInfoList) {
            val displayName = subscriptionInfo.displayName
            res[subscriptionInfo.simSlotIndex] = displayName.toString()
        }

        return res.map { it!! }.toTypedArray()
    }



    private fun getTimestamp(): String{
        return sdf.format(Date())
    }
}