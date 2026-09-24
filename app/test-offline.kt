import com.batoulapps.adhan.*
import com.batoulapps.adhan.data.DateComponents
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Locale

fun main() {
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
    println("Asr: " + formatter.format(prayerTimes.asr))
    
    params.madhab = Madhab.HANAFI
    val prayerTimesHanafi = PrayerTimes(coordinates, date, params)
    println("=== ASIA/KOLKATA (HANAFI) ===")
    println("Asr: " + formatter.format(prayerTimesHanafi.asr))
}
