package ru.firmachi.mobileapp.services

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.telephony.*
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.firmachi.mobileapp.ApiService
import ru.firmachi.mobileapp.App
import ru.firmachi.mobileapp.models.CellData
import ru.firmachi.mobileapp.models.api.SendCellDataRequest
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class TrackingService : Service() {


    private val delayInSeconds = 25 * 1000L
    private val requiredTaskCount = 30

    private val cellDataLocalRepository = App.component.getCellDataLocalRepository()
    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK)
    private val handler = Handler()

    private lateinit var runnable: Runnable

    private lateinit var telephonyManager: TelephonyManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var locationCallback: LocationCallback

    @Inject
    lateinit var apiService: ApiService

    init {
        App.component.inject(this)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as (TelephonyManager)
        subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        runnable = Runnable {
            runTask()
            handler.postDelayed(runnable, delayInSeconds)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TRACK", "start")
        runnable.run()
        return START_STICKY
    }


    private fun runTask(){
        Log.d("TRACK", "runTask")
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it == null || it.accuracy > 100) {
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                        if (locationResult != null && locationResult.locations.isNotEmpty()) {
                            retrieveCellData(locationResult.locations[0])
                        }
                    }
                }

                fusedLocationClient.requestLocationUpdates(LocationRequest(), locationCallback, null)
            } else {
                retrieveCellData(it)
            }
        }
    }


    private fun retrieveCellData(location: Location?){
        val cellDataToSave = mutableListOf<CellData>()
        val operatorsName = getOperatorsName()

        if(location != null && permissionsGranted()){
            val activeSim = telephonyManager.allCellInfo.filter { c -> c.isRegistered }
            for(i in activeSim.indices){
                val cellData = CellData()
                when(val cellInfo = activeSim[i]) {
                    is CellInfoGsm -> {
                        val d = cellInfo.cellSignalStrength.dbm
                        cellData.level = cellInfo.cellSignalStrength.level
                        cellData.cellType = "2G"
                    }
                    is CellInfoCdma -> {
                        cellData.level = cellInfo.cellSignalStrength.level
                        cellData.cellType = "3G"
                    }
                    is CellInfoWcdma -> {
                        cellData.level = cellInfo.cellSignalStrength.level
                        cellData.cellType = "3G"
                    }
                    is CellInfoLte -> {
                        cellData.level = cellInfo.cellSignalStrength.level
                        cellData.cellType = "4G"
                    }
                    else -> {
                        cellData.level = 0
                        cellData.cellType = "2G"
                    }
                }

                cellData.latitude = location.latitude
                cellData.longitude = location.latitude
                cellData.operatorName = operatorsName[i]
                cellData.timestamp = getTimestamp()
                cellData.imei = telephonyManager.getDeviceId(i)

                cellDataToSave.add(cellData)
            }

            cellDataLocalRepository.saveCellData(cellDataToSave)
            if(cellDataLocalRepository.getAllCellDataCount() > requiredTaskCount){
                syncData()
            }
        }
    }


    private fun permissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
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

    private fun syncData() {
        Log.d("TRACK", "Sync started")
        val cellData = cellDataLocalRepository
            .getAllCellData()
            .map {
                SendCellDataRequest(
                    it.latitude,
                    it.longitude,
                    it.cellType,
                    it.operatorName,
                    it.level,
                    it.timestamp,
                    it.imei
                )
            }.toList()

        if(cellData.isNotEmpty()){
            GlobalScope.launch(Dispatchers.IO) {
                val response = apiService.sendCellData(cellData).execute()

                if(response.isSuccessful && response.body()?.success == true){
                    cellDataLocalRepository.clearAll()
                    Log.d("TRACK", "Synced successfully")
                }
            }
        }
    }
}