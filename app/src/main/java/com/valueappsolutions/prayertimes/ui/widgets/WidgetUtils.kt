package com.valueappsolutions.prayertimes.ui.widgets

import com.valueappsolutions.prayertimes.domain.PrayerData
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object WidgetUtils {
    fun parseTime(timeStr: String): LocalTime? {
        val format = DateTimeFormatter.ofPattern("HH:mm")
        return try {
            LocalTime.parse(timeStr.split(" ")[0], format)
        } catch (e: Exception) {
            null
        }
    }

    fun formatDisplayTime(localTime: LocalTime?, is24HourFormat: Boolean): String {
        if (localTime == null) return ""
        return if (is24HourFormat) {
            localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } else {
            localTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        }
    }

    fun formatDisplayTimeStr(timeStr: String, is24HourFormat: Boolean): String {
        val parsed = parseTime(timeStr) ?: return timeStr
        return formatDisplayTime(parsed, is24HourFormat)
    }

    fun getSortedPrayers(data: PrayerData): List<Pair<String, LocalTime>> {
        return listOf(
            "Fajr" to data.fajr,
            "Sunrise" to data.sunrise,
            "Ishraq" to data.ishraq,
            "Chasht" to data.chasht,
            "Dhuhr" to data.dhuhr,
            "Asr" to data.asr,
            "Maghrib" to data.maghrib,
            "Isha" to data.isha,
            "Tahajjud" to data.tahajjud
        ).mapNotNull {
            val t = parseTime(it.second)
            if (t != null) it.first to t else null
        }.sortedBy { it.second }
    }

    fun getCurrentAndNextPrayer(
        allPrayers: List<Pair<String, LocalTime>>,
        currentTime: LocalTime
    ): Triple<String, String, LocalTime?> {
        var currentPrayer = allPrayers.lastOrNull()?.first ?: ""
        var nextPrayer = allPrayers.firstOrNull()?.first ?: ""
        var nextPrayerTime: LocalTime? = allPrayers.firstOrNull()?.second

        if (allPrayers.isNotEmpty()) {
            for (i in allPrayers.indices) {
                if (currentTime.isBefore(allPrayers[i].second)) {
                    if (i > 0) {
                        currentPrayer = allPrayers[i - 1].first
                    } else {
                        currentPrayer = allPrayers.last().first
                    }
                    nextPrayer = allPrayers[i].first
                    nextPrayerTime = allPrayers[i].second
                    break
                }
                if (i == allPrayers.size - 1) {
                    currentPrayer = allPrayers.last().first
                    nextPrayer = allPrayers.first().first
                    nextPrayerTime = allPrayers.first().second
                }
            }
        }
        return Triple(currentPrayer, nextPrayer, nextPrayerTime)
    }



    fun hasCrossedMaghrib(lastUpdatedTime: Long, maghribTimeStr: String): Boolean {
        if (lastUpdatedTime <= 0) return false
        return try {
            val maghribTime = parseTime(maghribTimeStr) ?: return false
            val lastUpdatedZdt = java.time.ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(lastUpdatedTime), java.time.ZoneId.systemDefault())
            val now = LocalTime.now()
            if (lastUpdatedZdt.toLocalDate() == java.time.LocalDate.now()) {
                lastUpdatedZdt.toLocalTime().isBefore(maghribTime) && !now.isBefore(maghribTime)
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
