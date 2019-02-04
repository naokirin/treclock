package com.nkrin.treclock.view.detail

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import com.nkrin.treclock.util.time.TimeProvider
import com.nkrin.treclock.view.alarm.Alarm
import com.nkrin.treclock.view.alarm.AlarmPlayer
import com.nkrin.treclock.view.notification.Notification
import com.nkrin.treclock.view.notification.NotificationReceiver
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class DetailAlarmManager(
    private val detailViewModel: DetailViewModel,
    private val alarmManager: AlarmManager,
    private val timeProvider: TimeProvider,
    private val context: Context
) {
    fun setAlarm(title: String, duration: Duration?, actualStart: OffsetDateTime?) {
        val intentSetup = { intent: Intent ->
            with(intent) {
                if (duration != null && actualStart != null) {
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    val endMessage = formatter.format(actualStart + duration)
                    putExtra(
                        "message", "〜${endMessage}"
                    )
                } else if (duration == null) {
                    putExtra("message", title)
                }
                putExtra(
                    "title",
                    "$title [${detailViewModel.schedule?.name ?: ""}]"
                )
            }
        }
        val alarm = Alarm(
            AlarmPlayer.createNewRequestCode(),
            actualStart as OffsetDateTime,
            timeProvider.now(),
            Notification()::notify,
            intentSetup,
            context,
            NotificationReceiver::class.java,
            alarmManager
        )

        AlarmPlayer.setUp("${detailViewModel.schedule?.id}", alarm)
    }

    fun stopAllAlarms() {
        AlarmPlayer.cancel("${detailViewModel.schedule?.id}")
        Notification().cancelAll(context)
    }
}