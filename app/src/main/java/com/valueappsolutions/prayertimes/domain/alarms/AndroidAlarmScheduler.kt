package com.valueappsolutions.prayertimes.domain.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.ZoneId

class AndroidAlarmScheduler(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(item: AlarmItem) {
        val targetClass = when (item.type) {
            1 -> SilentModeAlarmReceiver::class.java
            2 -> SilentModeEndReceiver::class.java
            else -> PrayerAlarmReceiver::class.java
        }
        val intent = Intent(context, targetClass).apply {
            putExtra("EXTRA_PRAYER_NAME", item.prayerName)
            putExtra("EXTRA_ID", item.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeMillis = item.time.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        val showIntent = Intent(context, com.valueappsolutions.prayertimes.MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            item.id,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(timeMillis, showPendingIntent),
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(timeMillis, showPendingIntent),
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun cancel(item: AlarmItem) {
        val targetClass = when (item.type) {
            1 -> SilentModeAlarmReceiver::class.java
            2 -> SilentModeEndReceiver::class.java
            else -> PrayerAlarmReceiver::class.java
        }
        val intent = Intent(context, targetClass).apply {
            putExtra("EXTRA_PRAYER_NAME", item.prayerName)
            putExtra("EXTRA_ID", item.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}
