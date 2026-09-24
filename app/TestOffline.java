import com.batoulapps.adhan.*;
import com.batoulapps.adhan.data.DateComponents;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TestOffline {
    public static void main(String[] args) {
        double lat = 28.6139; // New Delhi
        double lon = 77.2090;
        Coordinates coordinates = new Coordinates(lat, lon);
        DateComponents date = new DateComponents(2026, 9, 20);
        
        CalculationParameters params = CalculationMethod.KARACHI.getParameters();
        params.madhab = Madhab.SHAFI;
        PrayerTimes prayerTimes = new PrayerTimes(coordinates, date, params);
        
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        
        System.out.println("=== ASIA/KOLKATA ===");
        System.out.println("Fajr: " + formatter.format(prayerTimes.fajr));
        System.out.println("Asr: " + formatter.format(prayerTimes.asr));
        
        params.madhab = Madhab.HANAFI;
        PrayerTimes prayerTimesHanafi = new PrayerTimes(coordinates, date, params);
        System.out.println("=== ASIA/KOLKATA (HANAFI) ===");
        System.out.println("Asr: " + formatter.format(prayerTimesHanafi.asr));
    }
}
