package ru.firmachi.mobileapp.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StartServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.startService(Intent(context, TrackingService::class.java))
    }
}