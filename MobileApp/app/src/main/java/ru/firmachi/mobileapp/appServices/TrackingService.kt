package ru.firmachi.mobileapp.appServices

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.telephony.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.firmachi.mobileapp.services.ApiService
import ru.firmachi.mobileapp.App
import ru.firmachi.mobileapp.R
import ru.firmachi.mobileapp.models.CellData
import ru.firmachi.mobileapp.models.api.SendCellDataRequest
import ru.firmachi.mobileapp.services.NetworkService
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class TrackingService : Service() {

    companion object{
        const val notificationId = 1111
        var stopFlag = false
    }

    private val notificationChanel = "default"

    private val delayInSeconds = 30 * 1000L
    private val requiredTaskCount = 20

    private val cellDataLocalRepository = App.component.getCellDataLocalRepository()
    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK)
    private val handler = Handler()

    private lateinit var runnable: Runnable

    private lateinit var telephonyManager: TelephonyManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var locationCallback: LocationCallback

    private lateinit var networkService: NetworkService

    @Inject
    lateinit var apiService: ApiService

    init {
        App.component.inject(this)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.d("TRACK", "onCreate")
        super.onCreate()

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as (TelephonyManager)
        subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        networkService = NetworkService(applicationContext, telephonyManager, fusedLocationClient, subscriptionManager)

        runnable = Runnable {
            runTask()
            handler.postDelayed(runnable, delayInSeconds)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TRACK", "start")
        showNotification()
        runnable.run()
        return START_STICKY
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("TRACK", "TASK REMOVED")
        super.onTaskRemoved(rootIntent)
        if(stopFlag){
            stopFlag = false
            Log.d("TRACK", "taskKilled")
            return
        }

        sendBroadcast(Intent(applicationContext, StartServiceReceiver::class.java).setAction("StartServiceReceiver"))
        Log.d("TRACK", "taskRestarted")
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("TRACK", "onDestroy")
    }


    private fun runTask(){
        Toast.makeText(applicationContext, "runTask", Toast.LENGTH_SHORT).show()
        Log.d("TRACK", "runTask")
        networkService.getCellData {
            cellDataLocalRepository.saveCellData(it)
            if(cellDataLocalRepository.getAllCellDataCount() > requiredTaskCount){
                syncData()
            }
        }

//        fusedLocationClient.lastLocation.addOnSuccessListener {
//            if (it == null || it.accuracy > 100) {
//                locationCallback = object : LocationCallback() {
//                    override fun onLocationResult(locationResult: LocationResult?) {
//                        fusedLocationClient.removeLocationUpdates(locationCallback)
//                        if (locationResult != null && locationResult.locations.isNotEmpty()) {
//                            retrieveCellData(locationResult.locations[0])
//                        }
//                    }
//                }
//
//                fusedLocationClient.requestLocationUpdates(LocationRequest(), locationCallback, null)
//            } else {
//                retrieveCellData(it)
//            }
//        }
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


    private fun showNotification(){
        val snoozeIntent = Intent(baseContext, StopServiceReceiver::class.java).setAction("StopServiceReceiver")
        val snoozePendingIntent = PendingIntent.getBroadcast(baseContext, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        initChannels(baseContext)

        val builder = NotificationCompat.Builder(baseContext, notificationChanel)
            .setSmallIcon(R.mipmap.logo)
            .setContentTitle("Анализ качества сети")
            .setContentText("Сбор информации о силе сигнала мобильной сети")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(snoozePendingIntent)
            .addAction(
                R.mipmap.logo,
                "Отключить",
                snoozePendingIntent
            )
            .setOngoing(true)


        with(NotificationManagerCompat.from(baseContext)){
            notify(notificationId, builder.build())
            Log.d("TRACK", "notify")
        }
    }


    private fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            notificationChanel,
            this.javaClass.name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Channel description"
        notificationManager.createNotificationChannel(channel)
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