package com.valueappsolutions.prayertimes.domain.alarms

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import com.valueappsolutions.prayertimes.data.local.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class SilentModeAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("EXTRA_PRAYER_NAME") ?: return
        val id = intent.getIntExtra("EXTRA_ID", 0)

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PrayerTimes::SilentModeWakeLock"
        )
        wakeLock.acquire(15_000L) // 15 seconds

        val pendingResult = goAsync()
        val dataStoreManager = UserPreferencesRepository(context)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = dataStoreManager.userSettingsFlow.first()
                if (!settings.silentModeEnabled) return@launch

                // Save previous state only if it hasn't been saved yet
                if (settings.previousRingerMode == -1) {
                    val previousRingerMode = audioManager.ringerMode
                    dataStoreManager.updatePreviousRingerMode(previousRingerMode)
                }

                if (settings.previousInterruptionFilter == -1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val previousInterruptionFilter = notificationManager.currentInterruptionFilter
                    dataStoreManager.updatePreviousInterruptionFilter(previousInterruptionFilter)
                }

                // Check permissions before changing DND / Ringer Mode
                val hasDndPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    notificationManager.isNotificationPolicyAccessGranted
                } else {
                    true
                }

                if (hasDndPermission) {
                    when (settings.silentModeMuteType) {
                        0 -> audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                        1 -> {
                            // For DND, set Interruption Filter
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                            } else {
                                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                            }
                        }
                        2 -> audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    }
                } else {
                    // Fallback to Vibrate if we don't have DND permissions
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                }

                // Schedule the End Receiver dynamically
                val scheduler = AndroidAlarmScheduler(context)
                val duration = when (prayerName) {
                    "Fajr" -> settings.silentModeFajrDuration
                    "Dhuhr" -> settings.silentModeDhuhrDuration
                    "Asr" -> settings.silentModeAsrDuration
                    "Maghrib" -> settings.silentModeMaghribDuration
                    "Isha" -> settings.silentModeIshaDuration
                    "Jumah" -> settings.silentModeJumahDuration
                    else -> 10
                }.toLong()
                val endId = id + 10 // 21-25 for End alarms
                val endTime = LocalDateTime.now().plusMinutes(duration)
                scheduler.schedule(AlarmItem(endId, endTime, prayerName, type = 2))
            } finally {
                pendingResult.finish()
            }
        }
    }
}
