package com.valueappsolutions.prayertimes.domain.alarms

import android.content.Context
import com.valueappsolutions.prayertimes.data.local.UserSettings
import com.valueappsolutions.prayertimes.domain.PrayerData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmManagerHelper(context: Context) {
    private val scheduler = AndroidAlarmScheduler(context)

    fun updateAlarms(
        prayerData: PrayerData?,
        settings: UserSettings
    ) {
        val basePrayerNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha", "Jumah")
        // Cancel all existing alarms first (IDs 1-6 for Fajr-Jumah, 11-16 for Silent Mode)
        for (i in 1..6) {
            scheduler.cancel(AlarmItem(i, LocalDateTime.now(), basePrayerNames[i - 1]))
            scheduler.cancel(AlarmItem(i + 10, LocalDateTime.now(), basePrayerNames[i - 1], type = 1))
        }
        // Cancel Ayyam-e-Beed alarm (ID 20)
        scheduler.cancel(AlarmItem(20, LocalDateTime.now(), "Ayyam-e-Beed Reminder"))

        val today = LocalDate.now()
        val now = LocalDateTime.now()

        if (settings.notificationsEnabled) {
            val timesToSchedule = mutableMapOf<Int, LocalDateTime>()

            if (settings.notificationMode == 1) { // Manual Mode
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val manualSettings = listOf(
                    settings.notificationFajrEnabled to settings.manualFajrTime,
                    settings.notificationDhuhrEnabled to settings.manualDhuhrTime,
                    settings.notificationAsrEnabled to settings.manualAsrTime,
                    settings.notificationMaghribEnabled to settings.manualMaghribTime,
                    settings.notificationIshaEnabled to settings.manualIshaTime,
                    settings.notificationJumahEnabled to settings.manualJumahTime
                )
                manualSettings.forEachIndexed { index, (isEnabled, timeStr) ->
                    if (isEnabled && timeStr.isNotBlank()) {
                        try {
                            timesToSchedule[index + 1] = LocalDateTime.of(today, LocalTime.parse(timeStr, formatter))
                        } catch (e: Exception) {
                            // Ignore parse errors
                        }
                    }
                }
            } else { // Auto Mode
                if (prayerData != null) {
                    val autoSettings = listOf(
                        settings.notificationFajrEnabled to settings.notificationAutoFajrMinutes,
                        settings.notificationDhuhrEnabled to settings.notificationAutoDhuhrMinutes,
                        settings.notificationAsrEnabled to settings.notificationAutoAsrMinutes,
                        settings.notificationMaghribEnabled to settings.notificationAutoMaghribMinutes,
                        settings.notificationIshaEnabled to settings.notificationAutoIshaMinutes,
                        settings.notificationJumahEnabled to settings.notificationAutoJumahMinutes
                    )
                    val prayerTimes = listOf(
                        prayerData.fajr, prayerData.dhuhr, prayerData.asr,
                        prayerData.maghrib, prayerData.isha, prayerData.dhuhr
                    )
                    autoSettings.forEachIndexed { index, (isEnabled, minutes) ->
                        if (isEnabled) {
                            val parsedTime = if (settings.timeFormat == 1) {
                                LocalTime.parse(prayerTimes[index], DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US))
                            } else {
                                LocalTime.parse(prayerTimes[index].substringBefore(" "), DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.US))
                            }
                            timesToSchedule[index + 1] = LocalDateTime.of(today, parsedTime).plusMinutes(minutes.toLong())
                        }
                    }
                }
            }

            timesToSchedule.forEach { (id, time) ->
                var scheduleTime = time
                // If the time has already passed today, schedule for tomorrow
                if (scheduleTime.isBefore(now)) {
                    scheduleTime = scheduleTime.plusDays(1)
                }

                val isFriday = scheduleTime.dayOfWeek == java.time.DayOfWeek.FRIDAY
                if (id == 2 && isFriday) return@forEach // Skip Dhuhr on Friday
                if (id == 6 && !isFriday) return@forEach // Skip Jumah on non-Friday

                scheduler.schedule(AlarmItem(id, scheduleTime, basePrayerNames[id - 1]))
            }
        }

        // Silent Mode Scheduling
        if (settings.silentModeEnabled) {
            val silentTimesToSchedule = mutableMapOf<Int, LocalDateTime>()

            if (settings.silentModeType == 1) { // Manual Mode
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val manualSettings = listOf(
                    settings.silentModeFajrEnabled to settings.silentModeManualFajrTime,
                    settings.silentModeDhuhrEnabled to settings.silentModeManualDhuhrTime,
                    settings.silentModeAsrEnabled to settings.silentModeManualAsrTime,
                    settings.silentModeMaghribEnabled to settings.silentModeManualMaghribTime,
                    settings.silentModeIshaEnabled to settings.silentModeManualIshaTime,
                    settings.silentModeJumahEnabled to settings.silentModeManualJumahTime
                )
                manualSettings.forEachIndexed { index, (isEnabled, timeStr) ->
                    if (isEnabled && timeStr.isNotBlank()) {
                        try {
                            silentTimesToSchedule[index + 11] = LocalDateTime.of(today, LocalTime.parse(timeStr, formatter))
                        } catch (e: Exception) {
                            // Ignore parse errors
                        }
                    }
                }
            } else { // Auto Mode
                if (prayerData != null) {
                    val autoSettings = listOf(
                        settings.silentModeFajrEnabled to settings.silentModeAutoFajrMinutes,
                        settings.silentModeDhuhrEnabled to settings.silentModeAutoDhuhrMinutes,
                        settings.silentModeAsrEnabled to settings.silentModeAutoAsrMinutes,
                        settings.silentModeMaghribEnabled to settings.silentModeAutoMaghribMinutes,
                        settings.silentModeIshaEnabled to settings.silentModeAutoIshaMinutes,
                        settings.silentModeJumahEnabled to settings.silentModeAutoJumahMinutes
                    )
                    val prayerTimes = listOf(
                        prayerData.fajr, prayerData.dhuhr, prayerData.asr,
                        prayerData.maghrib, prayerData.isha, prayerData.dhuhr
                    )
                    autoSettings.forEachIndexed { index, (isEnabled, minutes) ->
                        if (isEnabled) {
                            val parsedTime = if (settings.timeFormat == 1) {
                                LocalTime.parse(prayerTimes[index], DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US))
                            } else {
                                LocalTime.parse(prayerTimes[index].substringBefore(" "), DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.US))
                            }
                            silentTimesToSchedule[index + 11] = LocalDateTime.of(today, parsedTime).plusMinutes(minutes.toLong())
                        }
                    }
                }
            }

            silentTimesToSchedule.forEach { (id, time) ->
                var scheduleTime = time
                if (scheduleTime.isBefore(now)) {
                    scheduleTime = scheduleTime.plusDays(1)
                }
                
                val isFriday = scheduleTime.dayOfWeek == java.time.DayOfWeek.FRIDAY
                if (id == 12 && isFriday) return@forEach // Skip Dhuhr on Friday
                if (id == 16 && !isFriday) return@forEach // Skip Jumah on non-Friday

                // type 1 = Silent Start
                scheduler.schedule(AlarmItem(id, scheduleTime, basePrayerNames[id - 11], type = 1))
            }
        }

        // Ayyam-e-Beed Reminder Scheduling
        if (settings.ayyamEBeedReminderEnabled && prayerData != null) {
            // Ayyam-e-Beed fasting starts on the 13th of the Hijri month. 
            // We want to remind a day before, which is the 12th.
            if (prayerData.hijriDay == 12) {
                try {
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    var scheduleTime = LocalDateTime.of(today, LocalTime.parse(settings.ayyamEBeedReminderTime, formatter))
                    if (scheduleTime.isBefore(now)) {
                        // Already passed for today (the 12th)
                        // In reality, if it's passed, it shouldn't ring. But if we add a day, it would ring on the 13th, 
                        // which is not "a day before". So if it's passed, we don't schedule it for tomorrow.
                        // However, just to be safe, if the time hasn't passed, we schedule it.
                    }
                    if (!scheduleTime.isBefore(now)) {
                        scheduler.schedule(AlarmItem(20, scheduleTime, "Ayyam-e-Beed Reminder"))
                    }
                } catch (e: Exception) {
                    // Ignore parse errors
                }
            }
        }
    }
}
