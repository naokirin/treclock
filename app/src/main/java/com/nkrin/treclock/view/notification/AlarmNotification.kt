package com.nkrin.treclock.view.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmNotification : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Notification().notify(context, intent)
    }
}