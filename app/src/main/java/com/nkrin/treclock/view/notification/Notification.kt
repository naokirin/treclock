package com.nkrin.treclock.view.notification

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.nkrin.treclock.R


class Notification(private val context: Context) {

    fun notify(intent: Intent) {
        val requestCode = intent.getIntExtra("request_code", 0)
        val message = intent.getStringExtra("message")
        val title = intent.getStringExtra("title")
        val pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "default"
        val appName = context.getString(R.string.app_name)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)

        if (Build.VERSION.SDK_INT >= 26) {
            setNotificationChannel(channelId, appName, message)
        }

        if (notificationManager is NotificationManager) {
            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setCategory(Notification.CATEGORY_ALARM)
                .build()

            notificationManager.notify(R.string.app_name, notification)
        }
    }

    fun cancelAll() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
        if (notificationManager is NotificationManager) {
            notificationManager.cancelAll()
        }
    }

    @TargetApi(26)
    private fun setNotificationChannel(channelId: String, appName: String, message: String) {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            .build()
        val channel = NotificationChannel(
            channelId, appName, NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = message
            enableVibration(true)
            canShowBadge()
            enableLights(true)
            lightColor = Color.BLUE
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(defaultSoundUri, audioAttributes)
            setShowBadge(true)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
        if (notificationManager is NotificationManager) {
            notificationManager.createNotificationChannel(channel)
        }
    }
}