import java.time.*
import java.time.chrono.*
import java.time.temporal.ChronoUnit
import org.shredzone.commons.suncalc.*

fun isMoonVisibleAtSunset(date: LocalDate, latitude: Double, longitude: Double, zoneId: ZoneId): Boolean {
    val zdt = date.atStartOfDay(zoneId)
    val sunTimes = SunTimes.compute().on(zdt).at(latitude, longitude).execute()
    val sunset = sunTimes.set ?: zdt.withHour(18)
    val sunsetIllumination = MoonIllumination.compute().on(sunset).execute()
    val sunsetPosition = MoonPosition.compute().on(sunset).at(latitude, longitude).execute()
    return sunsetIllumination.fraction >= 0.02 && sunsetPosition.altitude >= 5.0
}

fun getHijri(latitude: Double, longitude: Double, zoneIdStr: String) {
    val zoneId = ZoneId.of(zoneIdStr)
    val today = LocalDate.now(zoneId)
    val standardHijri = HijrahDate.from(today)
    val dayOfMonth = standardHijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
    val firstDayGregorian = today.minusDays(dayOfMonth.toLong() - 1)
    
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
    
    // Test original logic
    val yesterday = today.minusDays(1)
    val yesterdayZdt = yesterday.atStartOfDay(zoneId)
    val sunTimes = SunTimes.compute().on(yesterdayZdt).at(latitude, longitude).execute()
    val sunset = sunTimes.set ?: yesterdayZdt.withHour(18)
    val sunsetIllumination = MoonIllumination.compute().on(sunset).execute()
    val sunsetPosition = MoonPosition.compute().on(sunset).at(latitude, longitude).execute()
    val isVisible = sunsetIllumination.fraction >= 0.02 && sunsetPosition.altitude >= 5.0
    var oldCorrectedDate = standardHijri
    if (dayOfMonth == 1 && !isVisible) {
        oldCorrectedDate = standardHijri.minus(1, ChronoUnit.DAYS)
    } else if (dayOfMonth == 30 && isVisible) {
        oldCorrectedDate = standardHijri.plus(1, ChronoUnit.DAYS)
    }

    println("$zoneIdStr: Standard=$standardHijri, NewLocal=$correctedDate (diff=$diffDays), OldLocal=$oldCorrectedDate")
}

fun main() {
    println("Today: ${LocalDate.now()}")
    getHijri(21.4858, 39.1925, "Asia/Riyadh") // Jeddah
    getHijri(17.3850, 78.4867, "Asia/Kolkata") // Hyderabad
}
