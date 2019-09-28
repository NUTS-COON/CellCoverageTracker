package ru.firmachi.mobileapp.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

class StopServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("TRACK", "onReceiveStop")
        with(context?.let { NotificationManagerCompat.from(it) }){
            this?.cancel(TrackingService.notificationId)
            Log.d("TRACK", "notificationCanceled")
        }

        TrackingService.stopFlag = true
        context?.stopService(Intent(context, TrackingService::class.java))
    }
}