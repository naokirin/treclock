package com.nkrin.treclock.view.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import com.nkrin.treclock.R


class Notification {
    fun notify(context: Context, intent: Intent) {
        val requestCode = intent.getIntExtra("request_code", 0)
        val message = intent.getStringExtra("message")
        val title = intent.getStringExtra("title")
        val pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "default"
        val appName = context.getString(R.string.app_name)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val channel = NotificationChannel(
            channelId, appName, NotificationManager.IMPORTANCE_DEFAULT
        )
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            .build()
        channel.description = message
        channel.enableVibration(true)
        channel.canShowBadge()
        channel.enableLights(true)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        channel.setSound(defaultSoundUri, audioAttributes)
        channel.setShowBadge(true)

        if (notificationManager is NotificationManager) {
            notificationManager.createNotificationChannel(channel)
            val notification = Notification.Builder(context, channelId)
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

    fun cancelAll(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
        if (notificationManager is NotificationManager) {
            notificationManager.cancelAll()
        }
    }
}