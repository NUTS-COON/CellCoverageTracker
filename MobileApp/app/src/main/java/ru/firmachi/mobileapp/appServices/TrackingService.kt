package ru.firmachi.mobileapp.appServices

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.firmachi.mobileapp.App
import ru.firmachi.mobileapp.MainActivity
import ru.firmachi.mobileapp.R
import ru.firmachi.mobileapp.models.api.SendCellDataRequest
import ru.firmachi.mobileapp.services.ApiService
import ru.firmachi.mobileapp.services.NetworkService
import javax.inject.Inject


class TrackingService : Service() {

    companion object{
        const val notificationId = 1111
        var stopFlag = false
    }

    private var alreadyRun = false
    private val notificationChanel = "default"
    private val delayInSeconds = 20 * 1000L
    private val requiredTaskCount = 20
    private val infoNotificationId = 2222

    private val handler = Handler()
    private val cellDataLocalRepository = App.component.getCellDataLocalRepository()

    private lateinit var runnable: Runnable
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

        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as (TelephonyManager)
        val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        networkService = NetworkService(applicationContext, telephonyManager, fusedLocationClient, subscriptionManager)

        runnable = Runnable {
            retrieveCellData()
            handler.postDelayed(runnable, delayInSeconds)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(alreadyRun){
            Log.d("TRACK", "start_alreadyRun")
            return START_STICKY
        }

        alreadyRun = true
        Log.d("TRACK", "start")
        showNotification()

        runnable.run()
        val r = Runnable {
            showInfoNotification()
        }
        val h = Handler()
        h.postDelayed(r, 10 * 1000)

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


    private fun retrieveCellData(){
        Log.d("TRACK", "retrieveCellData")
        networkService.retrieveCellData {
            cellDataLocalRepository.saveCellData(it)
            if(cellDataLocalRepository.getAllCellDataCount() > requiredTaskCount){
                syncData()
            }
        }
    }


    private fun showInfoNotification(){
        val mainActivityIntent = Intent(baseContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(baseContext, 1, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        initChannels(baseContext)

        val builder = NotificationCompat.Builder(baseContext, notificationChanel)
            .setSmallIcon(R.mipmap.logo)
            .setContentTitle("Информация о сети")
            .setContentText("Приблизиетльно через 5 минут по маршруту вашего следования будет плохое покрытие сети в течении 30 минут")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        val notification = NotificationCompat.BigTextStyle(builder)
            .bigText("Приблизиетльно через 5 минут по маршруту вашего следования будет плохое покрытие сети в течении 30 минут")
            .build()


        with(NotificationManagerCompat.from(baseContext)){
            notify(infoNotificationId, notification)
            Log.d("TRACK", "notify")
        }
    }


    private fun showNotification(){
        val snoozeIntent = Intent(baseContext, StopServiceReceiver::class.java).setAction("StopServiceReceiver")
        val snoozePendingIntent = PendingIntent.getBroadcast(baseContext, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val mainActivityIntent = Intent(baseContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(baseContext, 1, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        initChannels(baseContext)

        val builder = NotificationCompat.Builder(baseContext, notificationChanel)
            .setSmallIcon(R.mipmap.logo)
            .setContentTitle("Анализ качества сети")
            .setContentText("Сбор информации о силе сигнала мобильной сети")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
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