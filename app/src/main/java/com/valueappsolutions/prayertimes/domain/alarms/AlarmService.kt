package com.valueappsolutions.prayertimes.domain.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.valueappsolutions.prayertimes.R
import com.valueappsolutions.prayertimes.data.local.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    companion object {
        const val ACTION_STOP_ALARM = "com.valueappsolutions.prayertimes.ACTION_STOP_ALARM"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ALARM) {
            stopSelf()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra("EXTRA_PRAYER_NAME") ?: "Prayer"
        val id = intent?.getIntExtra("EXTRA_ID", 100) ?: 100

        // Build Notification synchronously to ensure full-screen intent exemption is granted
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "prayer_times_channel_foreground"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Prayer Times (Alarms)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for prayer time alarms"
                setSound(null, null) // Sound is handled by the service
            }
            notificationManager.createNotificationChannel(channel)
        }

        val activityIntent = Intent(applicationContext, AlarmActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_PRAYER_NAME", prayerName)
            putExtra("EXTRA_ID", id)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            id,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(applicationContext, AlarmService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getService(
            applicationContext,
            id + 1000,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val isAyyamReminder = prayerName.contains("Ayyam-e-Beed", ignoreCase = true)
        val title = if (isAyyamReminder) "Ayyam-e-Beed Reminder" else "It's time for $prayerName"
        val text = if (isAyyamReminder) "Tomorrow is Ayyam-e-Beed fasting." else "Tap to open or dismiss to stop."

        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(pendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", stopPendingIntent)

        // Start foreground immediately
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(id, notificationBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(id, notificationBuilder.build())
        }

        serviceScope.launch {
            val dataStoreManager = UserPreferencesRepository(applicationContext)
            val settings = dataStoreManager.userSettingsFlow.first()
            
            if (settings.notificationType == 1 || settings.notificationType == 2) {
                // We can't update the vibration of the existing notification easily without notifying again,
                // but we can vibrate manually or update the notification
                notificationBuilder.setVibrate(longArrayOf(0, 500, 500, 500))
                notificationManager.notify(id, notificationBuilder.build())
            }

            // Play sound
            if (settings.notificationType == 0 || settings.notificationType == 2) {
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setWakeMode(applicationContext, android.os.PowerManager.PARTIAL_WAKE_LOCK)
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .build()
                        )
                        val afd = applicationContext.resources.openRawResourceFd(R.raw.azaan)
                        if (afd != null) {
                            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            afd.close()
                            prepare()
                            start()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Auto stop after 28 seconds
            delay(28_000)
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        serviceJob.cancel()
    }
}
