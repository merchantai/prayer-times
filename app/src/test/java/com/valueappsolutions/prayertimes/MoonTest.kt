package com.valueappsolutions.prayertimes

import org.junit.Test
import org.shredzone.commons.suncalc.SunTimes
import org.shredzone.commons.suncalc.MoonPosition
import org.shredzone.commons.suncalc.MoonIllumination
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

class MoonTest {

    private fun getDynamicHijriDate(
        latitude: Double,
        longitude: Double,
        offset: Int,
        destZoneIdStr: String
    ): HijrahDate {
        val zoneId = ZoneId.of(destZoneIdStr)
        val today = LocalDate.now(zoneId)
        val standardHijri = HijrahDate.from(today)
        
        val firstDayHijri = standardHijri.with(ChronoField.DAY_OF_MONTH, 1)
        val firstDayGregorian = LocalDate.from(firstDayHijri)

        fun isMoonVisibleAtSunset(date: LocalDate): Boolean {
            val dateZdt = date.atStartOfDay(zoneId)
            val sunTimes = SunTimes.compute().on(dateZdt).at(latitude, longitude).execute()
            val sunset = sunTimes.set ?: dateZdt.withHour(18)
            val sunsetIllumination = MoonIllumination.compute().on(sunset).execute()
            val sunsetPosition = MoonPosition.compute().on(sunset).at(latitude, longitude).execute()
            
            println("  Date: $date, fraction: ${sunsetIllumination.fraction}, altitude: ${sunsetPosition.altitude}")
            return sunsetIllumination.fraction >= 0.02 && sunsetPosition.altitude >= 5.0
        }

        println("Location: $destZoneIdStr ($latitude, $longitude)")
        println("Today Gregorian: $today")
        println("Standard Hijri: $standardHijri")
        println("First Day Gregorian: $firstDayGregorian")

        var localOffset = 0
        
        // Find when the moon first became visible around the Umm al-Qura start of month
        if (isMoonVisibleAtSunset(firstDayGregorian.minusDays(2))) {
            localOffset = 1
            println("Moon visible 2 days before. Offset = +1")
        } else if (isMoonVisibleAtSunset(firstDayGregorian.minusDays(1))) {
            localOffset = 0
            println("Moon visible 1 day before. Offset = 0")
        } else if (isMoonVisibleAtSunset(firstDayGregorian)) {
            localOffset = -1
            println("Moon visible on first day. Offset = -1")
        } else if (isMoonVisibleAtSunset(firstDayGregorian.plusDays(1))) {
            localOffset = -2
            println("Moon visible 1 day after. Offset = -2")
        } else {
            println("Moon not visible around these days? Defaulting to 0")
        }

        val finalOffset = localOffset + offset
        val correctedDate = standardHijri.plus(finalOffset.toLong(), java.time.temporal.ChronoUnit.DAYS)
        println("Corrected Hijri: $correctedDate\n")
        return correctedDate
    }

    @Test
    fun testLocations() {
        // Jeddah
        getDynamicHijriDate(21.4858, 39.1925, 0, "Asia/Riyadh")
        // Hyderabad
        getDynamicHijriDate(17.3850, 78.4867, 0, "Asia/Kolkata")
        // New York
        getDynamicHijriDate(40.7128, -74.0060, 0, "America/New_York")
        // Sydney
        getDynamicHijriDate(-33.8688, 151.2093, 0, "Australia/Sydney")
    }
}
