package ru.firmachi.mobileapp.appServices

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StartServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("TRACK", "onReceive")
        context?.startService(Intent(context, TrackingService::class.java))
    }
}