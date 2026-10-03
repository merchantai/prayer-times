package com.valueappsolutions.prayertimes.ui.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.valueappsolutions.prayertimes.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object SunTrajectoryUpdateScheduler {
    const val ACTION_UPDATE_SUN_WIDGET = "com.valueappsolutions.prayertimes.ACTION_UPDATE_SUN_WIDGET"
    private const val UPDATE_INTERVAL_MILLIS = 10 * 60 * 1000L // 10 minutes

    suspend fun scheduleNextUpdateIfActive(context: Context, forceImmediateUpdate: Boolean = false) {
        val repo = UserPreferencesRepository(context)
        val data = repo.cachedPrayerDataFlow.firstOrNull() ?: return

        val parse: (String) -> LocalTime? = { str ->
            try {
                val cleanStr = str.replace(Regex("[^0-9:]"), "")
                if (cleanStr.length == 4) LocalTime.parse("0$cleanStr", DateTimeFormatter.ofPattern("HH:mm"))
                else LocalTime.parse(cleanStr, DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e: Exception) { null }
        }
        val sunrise = parse(data.sunrise) ?: LocalTime.of(6, 0)
        val sunset = parse(data.maghrib) ?: LocalTime.of(18, 0)

        val now = LocalTime.now()

        // Only refresh between sunrise and sunset
        if (now.isAfter(sunrise) && now.isBefore(sunset)) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            
            // If screen is on (device is interactive)
            if (powerManager.isInteractive) {
                if (forceImmediateUpdate) {
                    val manager = GlanceAppWidgetManager(context)
                    val sunWidget = SunTrajectoryWidget()
                    manager.getGlanceIds(SunTrajectoryWidget::class.java).forEach { id ->
                        sunWidget.update(context, id)
                    }
                }
                
                // Schedule next update in 10 minutes
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, SunTrajectoryUpdateReceiver::class.java).apply {
                    action = ACTION_UPDATE_SUN_WIDGET
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context, 
                    1001, 
                    intent, 
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                alarmManager.cancel(pendingIntent)
                val triggerAtMillis = System.currentTimeMillis() + UPDATE_INTERVAL_MILLIS
                alarmManager.setExact(AlarmManager.RTC, triggerAtMillis, pendingIntent)
            }
        }
    }
}
