package com.valueappsolutions.prayertimes

import org.junit.Test
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonPosition
import org.shredzone.commons.suncalc.SunTimes
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoUnit

class MoonTest3 {

    fun isMoonVisibleAtSunset(date: LocalDate, latitude: Double, longitude: Double, zoneId: ZoneId): Boolean {
        val zdt = date.atStartOfDay(zoneId)
        val sunTimes = SunTimes.compute().on(zdt).at(latitude, longitude).execute()
        val sunset = sunTimes.set ?: zdt.withHour(18)
        val sunsetIllumination = MoonIllumination.compute().on(sunset).execute()
        val sunsetPosition = MoonPosition.compute().on(sunset).at(latitude, longitude).execute()
        return sunsetIllumination.fraction >= 0.02 && sunsetPosition.altitude >= 5.0
    }

    @Test
    fun testHijri() {
        val dates = listOf(
            LocalDate.of(2026, 8, 14), // Last month start
            LocalDate.of(2026, 9, 12)  // This month start
        )
        for (d in dates) {
            println("--- Date: $d ---")
            getHijri(d, 21.4858, 39.1925, "Asia/Riyadh") // Jeddah
            getHijri(d, 17.3850, 78.4867, "Asia/Kolkata") // Hyderabad
        }
    }

    fun getHijri(date: LocalDate, latitude: Double, longitude: Double, zoneIdStr: String) {
        val zoneId = ZoneId.of(zoneIdStr)
        val standardHijri = HijrahDate.from(date)
        val dayOfMonth = standardHijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
        val firstDayGregorian = date.minusDays(dayOfMonth.toLong() - 1)
        
        var localFirstDay: LocalDate? = null
        for (i in 0..3) {
            val checkDate = firstDayGregorian.minusDays(2).plusDays(i.toLong())
            if (isMoonVisibleAtSunset(checkDate, latitude, longitude, zoneId)) {
                localFirstDay = checkDate.plusDays(1)
                break
            }
        }
        if (localFirstDay == null) localFirstDay = firstDayGregorian
        val diffDays = ChronoUnit.DAYS.between(firstDayGregorian, localFirstDay).toInt()
        val correctedDate = standardHijri.minus(diffDays.toLong(), ChronoUnit.DAYS)
        println("$zoneIdStr: Standard=$standardHijri, NewLocal=$correctedDate (diff=$diffDays)")
    }
}
