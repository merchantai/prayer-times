package com.valueappsolutions.prayertimes

import org.junit.Test
import com.batoulapps.adhan.*
import com.batoulapps.adhan.data.DateComponents
import java.util.*
import java.text.SimpleDateFormat

class AdhanTest {
    @Test
    fun testAdhan() {
        val coordinates = Coordinates(24.8607, 67.0011)
        val date = DateComponents(2023, 10, 15)
        val params = CalculationMethod.KARACHI.parameters
        params.madhab = Madhab.HANAFI
        
        val prayerTimes = PrayerTimes(coordinates, date, params)
        val formatter = SimpleDateFormat("HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Karachi")
        }
        
        println("Fajr: " + formatter.format(prayerTimes.fajr))
        println("Sunrise: " + formatter.format(prayerTimes.sunrise))
        println("Dhuhr: " + formatter.format(prayerTimes.dhuhr))
        println("Asr: " + formatter.format(prayerTimes.asr))
        println("Maghrib: " + formatter.format(prayerTimes.maghrib))
        println("Isha: " + formatter.format(prayerTimes.isha))
    }
}
