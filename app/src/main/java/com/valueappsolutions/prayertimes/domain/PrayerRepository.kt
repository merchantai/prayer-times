package com.valueappsolutions.prayertimes.domain

import android.content.Context
import android.location.Geocoder
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.time.LocalDate
import kotlin.math.roundToInt
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.LocalTime
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale
import org.shredzone.commons.suncalc.SunTimes
import org.shredzone.commons.suncalc.MoonPosition
import org.shredzone.commons.suncalc.MoonIllumination

class PrayerRepository(
    private val context: Context
) {

    private data class CacheKey(
        val dateStr: String,
        val latitude: Double,
        val longitude: Double,
        val calculationMethod: Int,
        val asrMadhab: Int,
        val hijriOffset: Int,
        val tahajjudMethod: Int
    )
    private var cachedKey: CacheKey? = null
    private var cachedData: PrayerData? = null

    private fun getMoonFraction(
        latitude: Double,
        longitude: Double
    ): Double {
        val today = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        
        // Calculate moon fraction for today's current time for UI
        val nowZdt = ZonedDateTime.now(zoneId)
        val currentIllumination = MoonIllumination.compute().on(nowZdt).execute()
        
        return currentIllumination.fraction
    }
    
    // Kept for offline fallback
    private fun getDynamicHijriDate(
        latitude: Double,
        longitude: Double,
        offset: Int,
        destZoneIdStr: String
    ): HijrahDate {
        val zoneId = try { ZoneId.of(destZoneIdStr) } catch(e: Exception) { ZoneId.systemDefault() }
        val today = LocalDate.now(zoneId)
        val standardHijri = HijrahDate.from(today)
        
        val yesterday = today.minusDays(1)
        val yesterdayZdt = yesterday.atStartOfDay(zoneId)

        val sunTimes = SunTimes.compute()
            .on(yesterdayZdt)
            .at(latitude, longitude)
            .execute()

        val sunset = sunTimes.set ?: yesterdayZdt.withHour(18) // fallback if no sunset

        val sunsetIllumination = MoonIllumination.compute().on(sunset).execute()
        val sunsetPosition = MoonPosition.compute().on(sunset).at(latitude, longitude).execute()

        // Thresholds: 2.0% illumination and 5.0 degrees altitude
        val isVisible = sunsetIllumination.fraction >= 0.02 && sunsetPosition.altitude >= 5.0
        
        var correctedDate = standardHijri
        val dayOfMonth = standardHijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)

        if (dayOfMonth == 1 && !isVisible) {
            correctedDate = standardHijri.minus(1, ChronoUnit.DAYS)
        } else if (dayOfMonth == 30 && isVisible) {
            correctedDate = standardHijri.plus(1, ChronoUnit.DAYS)
        }

        if (offset != 0) {
            correctedDate = correctedDate.plus(offset.toLong(), ChronoUnit.DAYS)
        }

        return correctedDate
    }

    private fun calculateExtraPrayers(
        fajrStr: String,
        sunriseStr: String,
        maghribStr: String,
        tahajjudMethod: Int
    ): Triple<String, String, String> {
        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            
            val fajr = LocalTime.parse(fajrStr.split(" ")[0], formatter)
            val sunrise = LocalTime.parse(sunriseStr.split(" ")[0], formatter)
            val maghrib = LocalTime.parse(maghribStr.split(" ")[0], formatter)

            val ishraq = sunrise.plusMinutes(20)
            val chasht = sunrise.plusMinutes(45)

            var nightDuration = Duration.between(maghrib, fajr)
            if (nightDuration.isNegative) {
                nightDuration = nightDuration.plusHours(24)
            }
            
            val tahajjud = if (tahajjudMethod == 1) {
                maghrib.plus(nightDuration.dividedBy(2))
            } else {
                maghrib.plus(nightDuration.dividedBy(3).multipliedBy(2))
            }

            Triple(
                formatter.format(ishraq),
                formatter.format(chasht),
                formatter.format(tahajjud)
            )
        } catch (e: Exception) {
            Triple("", "", "")
        }
    }

    suspend fun resolveLocationName(latitude: Double, longitude: Double): Pair<String, String> = withContext(Dispatchers.IO) {
        var fetchedArea = ""
        var fetchedCity = ""
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                fetchedArea = address.subLocality ?: address.thoroughfare ?: ""
                fetchedCity = address.locality ?: address.subAdminArea ?: ""
            }
        } catch (e: Exception) {
            // Geocoding failed, ignore
        }
        Pair(fetchedArea, fetchedCity)
    }

    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        calculationMethod: Int = 2,
        asrMadhab: Int = 0,
        hijriOffset: Int = 0,
        tahajjudMethod: Int = 0,
        providedArea: String? = null,
        providedCity: String? = null,
        savedTimezoneId: String? = null
    ): PrayerData = withContext(Dispatchers.IO) {


        fun getLocalTimeStr(destTimeStr: String, destZoneIdStr: String): String {
            return try {
                if (destZoneIdStr.isEmpty() || destZoneIdStr == ZoneId.systemDefault().id) return destTimeStr
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val cleanTime = destTimeStr.split(" ")[0]
                val destTime = LocalTime.parse(cleanTime, formatter)
                val destZoned = ZonedDateTime.of(LocalDate.now(ZoneId.of(destZoneIdStr)), destTime, ZoneId.of(destZoneIdStr))
                val localZoned = destZoned.withZoneSameInstant(ZoneId.systemDefault())
                formatter.format(localZoned)
            } catch (e: Exception) {
                destTimeStr
            }
        }

        fun getApproximateTimeZone(lon: Double): String {
            val offsetHours = (lon / 15.0).roundToInt()
            val sign = if (offsetHours >= 0) "+" else "-"
            return String.format(Locale.US, "GMT%s%02d:00", sign, Math.abs(offsetHours))
        }

        val currentDateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
        val currentCacheKey = CacheKey(
            dateStr = currentDateStr,
            latitude = latitude,
            longitude = longitude,
            calculationMethod = calculationMethod,
            asrMadhab = asrMadhab,
            hijriOffset = hijriOffset,
            tahajjudMethod = tahajjudMethod
        )

        if (cachedKey == currentCacheKey && cachedData != null) {
            return@withContext cachedData!!
        }

        val area = providedArea ?: ""
        val city = providedCity ?: ""

        val destZoneIdStr = if (!savedTimezoneId.isNullOrEmpty()) {
            savedTimezoneId
        } else {
            getApproximateTimeZone(longitude)
        }
        val destZoneId = try { ZoneId.of(destZoneIdStr) } catch(e: Exception) { ZoneId.systemDefault() }
        val todayInDest = LocalDate.now(destZoneId)

        // Offline calculation using Adhan library
        val coordinates = Coordinates(latitude, longitude)
        val date = DateComponents(todayInDest.year, todayInDest.monthValue, todayInDest.dayOfMonth)
        
        // Map method accurately based on user's JSON data
        val params = when (calculationMethod) {
            0 -> CalculationParameters(16.0, 14.0) // Jafari
            1 -> CalculationMethod.KARACHI.parameters
            2 -> CalculationMethod.NORTH_AMERICA.parameters
            3 -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
            4 -> CalculationMethod.UMM_AL_QURA.parameters
            5 -> CalculationMethod.EGYPTIAN.parameters
            7 -> CalculationParameters(17.7, 14.0) // Tehran
            8 -> CalculationParameters(19.5, 90) // Gulf
            9 -> CalculationMethod.KUWAIT.parameters
            10 -> CalculationMethod.QATAR.parameters
            11 -> CalculationMethod.SINGAPORE.parameters
            12 -> CalculationParameters(12.0, 12.0) // UOIF
            13 -> CalculationParameters(18.0, 17.0) // Turkey
            14 -> CalculationParameters(16.0, 15.0) // Russia
            15 -> CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters
            16 -> CalculationMethod.DUBAI.parameters
            else -> CalculationMethod.NORTH_AMERICA.parameters
        }
        
        params.madhab = if (asrMadhab == 1) Madhab.HANAFI else Madhab.SHAFI

        val prayerTimes = PrayerTimes(coordinates, date, params)
        val isDiff = destZoneIdStr != ZoneId.systemDefault().id
        
        val destFormatter = SimpleDateFormat("HH:mm", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone(destZoneIdStr)
        }

        // Hijri fallback
        var totalOffset = hijriOffset
        try {
            if (Date().after(prayerTimes.maghrib)) {
                totalOffset += 1
            }
        } catch (e: Exception) {
            // Ignore
        }
        val hijrahDate = getDynamicHijriDate(latitude, longitude, totalOffset, destZoneIdStr)
        val moonFraction = getMoonFraction(latitude, longitude)
        val formatterHijri = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US)
        val hijriDateStr = formatterHijri.format(hijrahDate)
        val hijriDay = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
        val currentDateStrOffline = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())

        val fajrStr = destFormatter.format(prayerTimes.fajr)
        val sunriseStr = destFormatter.format(prayerTimes.sunrise)
        val maghribStr = destFormatter.format(prayerTimes.maghrib)

        val (ishraq, chasht, tahajjud) = calculateExtraPrayers(
            fajrStr = fajrStr,
            sunriseStr = sunriseStr,
            maghribStr = maghribStr,
            tahajjudMethod = tahajjudMethod
        )
        
        val finalDhuhr = destFormatter.format(prayerTimes.dhuhr)
        val finalAsr = destFormatter.format(prayerTimes.asr)
        val finalIsha = destFormatter.format(prayerTimes.isha)

        val result = PrayerData(
            fajr = fajrStr,
            sunrise = sunriseStr,
            ishraq = ishraq,
            chasht = chasht,
            dhuhr = finalDhuhr,
            asr = finalAsr,
            maghrib = maghribStr,
            isha = finalIsha,
            tahajjud = tahajjud,
            fajrLocal = if (isDiff) getLocalTimeStr(fajrStr, destZoneIdStr) else "",
            sunriseLocal = if (isDiff) getLocalTimeStr(sunriseStr, destZoneIdStr) else "",
            ishraqLocal = if (isDiff) getLocalTimeStr(ishraq, destZoneIdStr) else "",
            chashtLocal = if (isDiff) getLocalTimeStr(chasht, destZoneIdStr) else "",
            dhuhrLocal = if (isDiff) getLocalTimeStr(finalDhuhr, destZoneIdStr) else "",
            asrLocal = if (isDiff) getLocalTimeStr(finalAsr, destZoneIdStr) else "",
            maghribLocal = if (isDiff) getLocalTimeStr(maghribStr, destZoneIdStr) else "",
            ishaLocal = if (isDiff) getLocalTimeStr(finalIsha, destZoneIdStr) else "",
            tahajjudLocal = if (isDiff) getLocalTimeStr(tahajjud, destZoneIdStr) else "",
            isLocalTimeDifferent = isDiff,
            destinationTimezoneId = destZoneIdStr,
            hijriDate = hijriDateStr,
            hijriDay = hijriDay,
            currentDate = currentDateStrOffline,
            isOfflineFallback = true,
            areaName = area,
            cityName = city,
            moonFraction = moonFraction,
            calculationMethod = calculationMethod,
            asrMadhab = asrMadhab,
            hijriOffset = hijriOffset,
            tahajjudMethod = tahajjudMethod
        )
        
        cachedKey = currentCacheKey
        cachedData = result
        return@withContext result
    }
}
