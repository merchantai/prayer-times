package com.valueappsolutions.prayertimes.ui.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.valueappsolutions.prayertimes.data.local.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit


class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val preferencesRepository = UserPreferencesRepository(context)
            val cachedData = preferencesRepository.cachedPrayerDataFlow.firstOrNull()
            val settings = preferencesRepository.userSettingsFlow.firstOrNull()

            if (cachedData != null && settings != null) {
                val currentDateStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US).format(java.util.Date())
                val isDateChanged = cachedData.currentDate != currentDateStr
                
                val crossedMaghrib = WidgetUtils.hasCrossedMaghrib(
                    lastUpdatedTime = cachedData.lastUpdatedTime,
                    maghribTimeStr = cachedData.maghrib
                )

                if (isDateChanged || crossedMaghrib) {
                    try {
                        val prayerRepository = com.valueappsolutions.prayertimes.domain.PrayerRepository(context)
                        
                        val tz = if (settings.isAutomaticLocation) ZoneId.systemDefault().id else cachedData.destinationTimezoneId
                        val newData = prayerRepository.getPrayerTimes(
                            latitude = cachedData.latitude,
                            longitude = cachedData.longitude,
                            calculationMethod = settings.calculationMethod,
                            asrMadhab = settings.asrMadhab,
                            hijriOffset = settings.hijriOffset,
                            tahajjudMethod = settings.tahajjudMethod,
                            providedArea = cachedData.areaName,
                            providedCity = cachedData.cityName,
                            savedTimezoneId = tz
                        )
                        
                        val updatedData = newData.copy(
                            lastUpdatedTime = System.currentTimeMillis(),
                            latitude = cachedData.latitude,
                            longitude = cachedData.longitude
                        )
                        preferencesRepository.updateCachedPrayerData(updatedData)
                        
                        val alarmHelper = com.valueappsolutions.prayertimes.domain.alarms.AlarmManagerHelper(context)
                        alarmHelper.updateAlarms(updatedData, settings)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            WidgetUpdateScheduler.updateAllWidgets(context)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

object WidgetUpdateScheduler {
    private const val WIDGET_UPDATE_WORK_NAME = "WidgetUpdateWorker"

    suspend fun updateAllWidgets(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        
        val currentNextWidget = CurrentNextPrayerWidget()
        val largeFardWidget = LargeFardPrayersWidget()
        val horizontalFardWidget = HorizontalFardPrayersWidget()
        val hijriDateWidget = HijriDateWidget()

        manager.getGlanceIds(CurrentNextPrayerWidget::class.java).forEach { id ->
            currentNextWidget.update(context, id)
        }
        manager.getGlanceIds(LargeFardPrayersWidget::class.java).forEach { id ->
            largeFardWidget.update(context, id)
        }
        manager.getGlanceIds(HorizontalFardPrayersWidget::class.java).forEach { id ->
            horizontalFardWidget.update(context, id)
        }
        manager.getGlanceIds(HijriDateWidget::class.java).forEach { id ->
            hijriDateWidget.update(context, id)
        }

        scheduleNextUpdate(context)
    }

    suspend fun scheduleNextUpdate(context: Context) {
        val repo = UserPreferencesRepository(context)
        val data = repo.cachedPrayerDataFlow.firstOrNull() ?: return
        
        val sortedPrayers = WidgetUtils.getSortedPrayers(data)
        val now = LocalTime.now()
        val (_, _, nextTime) = WidgetUtils.getCurrentAndNextPrayer(sortedPrayers, now)
        
        if (nextTime != null) {
            var nextDateTime = LocalDateTime.of(java.time.LocalDate.now(), nextTime)
            if (nextDateTime.isBefore(LocalDateTime.now()) || nextDateTime.isEqual(LocalDateTime.now())) {
                nextDateTime = nextDateTime.plusDays(1)
            }
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WidgetUpdateReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            try {
                val triggerAtMillis = nextDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // Handle if exact alarms are not permitted by just falling back to the midnight update
                e.printStackTrace()
            }
        }
    }

    fun scheduleMidnightUpdate(context: Context) {
        val now = LocalDateTime.now()
        val midnight = LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.MIDNIGHT)
        var delay = Duration.between(now, midnight).toMinutes()
        if (delay < 0) delay = 0

        val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
