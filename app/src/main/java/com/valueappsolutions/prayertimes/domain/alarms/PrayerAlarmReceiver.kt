package com.valueappsolutions.prayertimes.domain.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.valueappsolutions.prayertimes.MainActivity
import com.valueappsolutions.prayertimes.R
import com.valueappsolutions.prayertimes.data.local.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("EXTRA_PRAYER_NAME") ?: return
        val id = intent.getIntExtra("EXTRA_ID", 0)

        val pendingResult = goAsync()
        val dataStoreManager = UserPreferencesRepository(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = dataStoreManager.userSettingsFlow.first()
                if (!settings.notificationsEnabled) return@launch

                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    putExtra("EXTRA_PRAYER_NAME", prayerName)
                    putExtra("EXTRA_ID", id)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
