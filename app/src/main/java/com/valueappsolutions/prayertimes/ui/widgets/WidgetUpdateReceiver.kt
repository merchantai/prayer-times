package com.valueappsolutions.prayertimes.ui.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.valueappsolutions.prayertimes.data.local.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.ZoneId

class WidgetUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PrayerTimes::WidgetUpdateWakeLock"
        )
        wakeLock.acquire(15_000L) // 15 seconds max

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
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
            } finally {
                pendingResult.finish()
            }
        }
    }
}
