package com.valueappsolutions.prayertimes.data.local

data class UserSettings(
    val calculationMethod: Int = 4, // Umm-al-Qura as default
    val asrMadhab: Int = 0, // 0 for Shafi/Standard, 1 for Hanafi
    val hijriOffset: Int = 0, // -2 to +2 days
    val tahajjudMethod: Int = 0, // 0 = Last Third, 1 = Middle of Night
    @Deprecated("Use themeMode instead")
    val isDarkMode: Boolean = true, // true for dark mode, false for light mode
    val themeMode: Int = 0, // 0 = System, 1 = Light, 2 = Dark
    val is24HourFormat: Boolean = false, // true for 24h, false for 12h
    val isAutomaticLocation: Boolean = true,
    val manualLatitude: Double = 0.0,
    val manualLongitude: Double = 0.0,
    val notificationsEnabled: Boolean = false,
    val notificationFajrEnabled: Boolean = true,
    val notificationDhuhrEnabled: Boolean = true,
    val notificationAsrEnabled: Boolean = true,
    val notificationMaghribEnabled: Boolean = true,
    val notificationIshaEnabled: Boolean = true,
    val notificationJumahEnabled: Boolean = true,
    val notificationMode: Int = 0, // 0 = Automatic, 1 = Manual
    val notificationAutoFajrMinutes: Int = 0,
    val notificationAutoDhuhrMinutes: Int = 0,
    val notificationAutoAsrMinutes: Int = 0,
    val notificationAutoMaghribMinutes: Int = 0,
    val notificationAutoIshaMinutes: Int = 0,
    val notificationAutoJumahMinutes: Int = 0,
    val notificationType: Int = 0, // 0 = Ring, 1 = Vibrate, 2 = Both
    val manualFajrTime: String = "",
    val manualDhuhrTime: String = "",
    val manualAsrTime: String = "",
    val manualMaghribTime: String = "",
    val manualIshaTime: String = "",
    val manualJumahTime: String = "",
    val silentModeEnabled: Boolean = false,
    val silentModeFajrEnabled: Boolean = true,
    val silentModeDhuhrEnabled: Boolean = true,
    val silentModeAsrEnabled: Boolean = true,
    val silentModeMaghribEnabled: Boolean = true,
    val silentModeIshaEnabled: Boolean = true,
    val silentModeJumahEnabled: Boolean = true,
    val silentModeFajrDuration: Int = 10,
    val silentModeDhuhrDuration: Int = 10,
    val silentModeAsrDuration: Int = 10,
    val silentModeMaghribDuration: Int = 10,
    val silentModeIshaDuration: Int = 10,
    val silentModeJumahDuration: Int = 10,
    val silentModeType: Int = 0, // 0 = Auto, 1 = Manual
    val silentModeMuteType: Int = 0, // 0 = Silent, 1 = DND, 2 = Vibrate
    val silentModeAutoFajrMinutes: Int = 0,
    val silentModeAutoDhuhrMinutes: Int = 0,
    val silentModeAutoAsrMinutes: Int = 0,
    val silentModeAutoMaghribMinutes: Int = 0,
    val silentModeAutoIshaMinutes: Int = 0,
    val silentModeAutoJumahMinutes: Int = 0,
    val silentModeManualFajrTime: String = "",
    val silentModeManualDhuhrTime: String = "",
    val silentModeManualAsrTime: String = "",
    val silentModeManualMaghribTime: String = "",
    val silentModeManualIshaTime: String = "",
    val silentModeManualJumahTime: String = "",
    val previousRingerMode: Int = -1,
    val previousInterruptionFilter: Int = -1,
    val ayyamEBeedReminderEnabled: Boolean = false,
    val ayyamEBeedReminderTime: String = "20:00" // HH:mm format, default 8:00 PM
)
