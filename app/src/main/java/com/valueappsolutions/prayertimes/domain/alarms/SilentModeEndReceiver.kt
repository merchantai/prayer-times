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

class SilentModeEndReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PrayerTimes::SilentModeEndWakeLock"
        )
        wakeLock.acquire(15_000L) // 15 seconds

        val pendingResult = goAsync()
        val dataStoreManager = UserPreferencesRepository(context)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = dataStoreManager.userSettingsFlow.first()

                val previousRingerMode = settings.previousRingerMode
                val previousInterruptionFilter = settings.previousInterruptionFilter

                if (previousRingerMode != -1 || previousInterruptionFilter != -1) {
                    // Check permissions before changing DND / Ringer Mode
                    val hasDndPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        notificationManager.isNotificationPolicyAccessGranted
                    } else {
                        true
                    }

                    if (hasDndPermission) {
                        // Restore previous Interruption Filter if available
                        if (previousInterruptionFilter != -1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            notificationManager.setInterruptionFilter(previousInterruptionFilter)
                            kotlinx.coroutines.delay(100) // Allow OS to process DND disable before setting ringer mode
                        } else if (settings.silentModeMuteType == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // Fallback if we didn't capture the filter, but muteType was DND
                            if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE) {
                                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                                kotlinx.coroutines.delay(100)
                            }
                        }

                        if (previousRingerMode != -1) {
                            audioManager.ringerMode = previousRingerMode
                        }
                    } else {
                        if (previousRingerMode != -1) {
                            audioManager.ringerMode = previousRingerMode
                        }
                    }

                    // Reset previous state
                    dataStoreManager.updatePreviousRingerMode(-1)
                    dataStoreManager.updatePreviousInterruptionFilter(-1)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
