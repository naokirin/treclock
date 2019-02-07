package com.nkrin.treclock.view.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.threeten.bp.OffsetDateTime

class Alarm(
    val requestCode: Int,
    private val actualStart: OffsetDateTime?,
    private val now: OffsetDateTime,
    private val callback: (Context, Intent) -> Unit,
    private val setupIntent: (Intent) -> Intent,
    private val context: Context,
    private val broadcastReceiverClass: Class<out Any?>,
    private val alarmManager: AlarmManager
) {
    fun setUp() {
        var intent = Intent(context, broadcastReceiverClass)
        intent.putExtra("request_code", requestCode)
        intent = setupIntent(intent)
        val pending = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        if (actualStart != null) {
            if (actualStart <= now) {
                callback(context, intent)
            } else {
                val millis = actualStart.toEpochSecond() * 1000L
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    millis,
                    pending
                )
            }
        }
    }

    fun cancel() {
        val intent = Intent(context, broadcastReceiverClass)
        val pending = PendingIntent.getBroadcast(
            context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pending)
    }
}
