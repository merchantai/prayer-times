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

    fun getDynamicHijriDateStr(
        latitude: Double,
        longitude: Double,
        offset: Int,
        maghribTimeStr: String
    ): String {
        var totalOffset = offset
        try {
            val maghribTime = parseTime(maghribTimeStr)
            if (maghribTime != null && !LocalTime.now().isBefore(maghribTime)) {
                totalOffset += 1
            }
        } catch (e: Exception) {
            // Ignore
        }

        val today = java.time.LocalDate.now()
        val standardHijri = java.time.chrono.HijrahDate.from(today)
        val zoneId = java.time.ZoneId.systemDefault()
        
        val yesterday = today.minusDays(1)
        val yesterdayZdt = yesterday.atStartOfDay(zoneId)

        val sunTimes = org.shredzone.commons.suncalc.SunTimes.compute()
            .on(yesterdayZdt)
            .at(latitude, longitude)
            .execute()

        val sunset = sunTimes.set ?: yesterdayZdt.withHour(18)

        val sunsetIllumination = org.shredzone.commons.suncalc.MoonIllumination.compute().on(sunset).execute()
        val sunsetPosition = org.shredzone.commons.suncalc.MoonPosition.compute().on(sunset).at(latitude, longitude).execute()

        val isVisible = sunsetIllumination.fraction >= 0.02 && sunsetPosition.altitude >= 5.0
        
        var correctedDate = standardHijri
        val dayOfMonth = standardHijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)

        if (dayOfMonth == 1 && !isVisible) {
            correctedDate = standardHijri.minus(1, java.time.temporal.ChronoUnit.DAYS)
        } else if (dayOfMonth == 30 && isVisible) {
            correctedDate = standardHijri.plus(1, java.time.temporal.ChronoUnit.DAYS)
        }

        if (totalOffset != 0) {
            correctedDate = correctedDate.plus(totalOffset.toLong(), java.time.temporal.ChronoUnit.DAYS)
        }

        val formatterHijri = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.US)
        return formatterHijri.format(correctedDate)
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
