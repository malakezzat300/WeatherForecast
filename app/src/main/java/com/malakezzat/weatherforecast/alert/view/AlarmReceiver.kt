package com.malakezzat.weatherforecast.alert.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "ACTION_DISMISS_ALARM") {
            context?.stopService(Intent(context, AlarmService::class.java))
        }
    }
}