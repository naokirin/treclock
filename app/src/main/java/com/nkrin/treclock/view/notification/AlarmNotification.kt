package com.nkrin.treclock.view.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import com.nkrin.treclock.R


class AlarmNotification : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val requestCode = intent.getIntExtra("request_code", 0)
        val message = intent.getStringExtra("message")
        val pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val channelId = "default"
        val title = context.getString(R.string.app_name)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val channel = NotificationChannel(
            channelId, title, NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = message
        channel.enableVibration(true)
        channel.canShowBadge()
        channel.enableLights(true)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        channel.setSound(defaultSoundUri, null)
        channel.setShowBadge(true)

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel)
            val notification = Notification.Builder(context, channelId)
                .setContentTitle(title)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .build()

            notificationManager.notify(R.string.app_name, notification)
        }
    }
}