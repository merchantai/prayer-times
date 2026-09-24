package com.valueappsolutions.prayertimes

import org.junit.Test
import com.batoulapps.adhan.*
import com.batoulapps.adhan.data.DateComponents
import java.util.Date
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Locale

class CalculationTest {
    @Test
    fun testCalculations() {
        val lat = 28.6139 // New Delhi
        val lon = 77.2090
        val coordinates = Coordinates(lat, lon)
        val date = DateComponents(2026, 9, 20)
        
        val params = CalculationMethod.KARACHI.parameters
        params.madhab = Madhab.SHAFI
        val prayerTimes = PrayerTimes(coordinates, date, params)
        
        val formatter = SimpleDateFormat("HH:mm", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        
        println("=== ASIA/KOLKATA ===")
        println("Fajr: " + formatter.format(prayerTimes.fajr))
        println("Sunrise: " + formatter.format(prayerTimes.sunrise))
        println("Dhuhr: " + formatter.format(prayerTimes.dhuhr))
        println("Asr: " + formatter.format(prayerTimes.asr))
        println("Maghrib: " + formatter.format(prayerTimes.maghrib))
        println("Isha: " + formatter.format(prayerTimes.isha))
        
        formatter.timeZone = TimeZone.getTimeZone("GMT+05:00")
        println("=== GMT+05:00 ===")
        println("Fajr: " + formatter.format(prayerTimes.fajr))
        println("Sunrise: " + formatter.format(prayerTimes.sunrise))
        println("Dhuhr: " + formatter.format(prayerTimes.dhuhr))
        println("Asr: " + formatter.format(prayerTimes.asr))
        println("Maghrib: " + formatter.format(prayerTimes.maghrib))
        println("Isha: " + formatter.format(prayerTimes.isha))
    }
}
