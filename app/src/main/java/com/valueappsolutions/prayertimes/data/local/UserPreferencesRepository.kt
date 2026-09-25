package com.valueappsolutions.prayertimes.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import com.valueappsolutions.prayertimes.domain.PrayerData
import com.valueappsolutions.prayertimes.domain.SavedLocation
import com.valueappsolutions.prayertimes.ui.widgets.WidgetUpdateScheduler

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {
    private val CALCULATION_METHOD = intPreferencesKey("calculation_method")
    private val ASR_MADHAB = intPreferencesKey("asr_madhab")
    private val HIJRI_OFFSET = intPreferencesKey("hijri_offset")
    private val TAHAJJUD_METHOD = intPreferencesKey("tahajjud_method")
    private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    private val THEME_MODE = intPreferencesKey("theme_mode")
    private val IS_24_HOUR_FORMAT = booleanPreferencesKey("is_24_hour_format")
    private val IS_AUTOMATIC_LOCATION = booleanPreferencesKey("is_automatic_location")
    private val MANUAL_LATITUDE = doublePreferencesKey("manual_latitude")
    private val MANUAL_LONGITUDE = doublePreferencesKey("manual_longitude")
    private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    private val NOTIFICATION_FAJR_ENABLED = booleanPreferencesKey("notification_fajr_enabled")
    private val NOTIFICATION_DHUHR_ENABLED = booleanPreferencesKey("notification_dhuhr_enabled")
    private val NOTIFICATION_ASR_ENABLED = booleanPreferencesKey("notification_asr_enabled")
    private val NOTIFICATION_MAGHRIB_ENABLED = booleanPreferencesKey("notification_maghrib_enabled")
    private val NOTIFICATION_ISHA_ENABLED = booleanPreferencesKey("notification_isha_enabled")
    private val NOTIFICATION_JUMAH_ENABLED = booleanPreferencesKey("notification_jumah_enabled")
    private val NOTIFICATION_MODE = intPreferencesKey("notification_mode")
    private val NOTIFICATION_AUTO_FAJR_MINUTES = intPreferencesKey("notification_auto_fajr_minutes")
    private val NOTIFICATION_AUTO_DHUHR_MINUTES = intPreferencesKey("notification_auto_dhuhr_minutes")
    private val NOTIFICATION_AUTO_ASR_MINUTES = intPreferencesKey("notification_auto_asr_minutes")
    private val NOTIFICATION_AUTO_MAGHRIB_MINUTES = intPreferencesKey("notification_auto_maghrib_minutes")
    private val NOTIFICATION_AUTO_ISHA_MINUTES = intPreferencesKey("notification_auto_isha_minutes")
    private val NOTIFICATION_AUTO_JUMAH_MINUTES = intPreferencesKey("notification_auto_jumah_minutes")
    private val NOTIFICATION_TYPE = intPreferencesKey("notification_type")
    private val MANUAL_FAJR_TIME = stringPreferencesKey("manual_fajr_time")
    private val MANUAL_DHUHR_TIME = stringPreferencesKey("manual_dhuhr_time")
    private val MANUAL_ASR_TIME = stringPreferencesKey("manual_asr_time")
    private val MANUAL_MAGHRIB_TIME = stringPreferencesKey("manual_maghrib_time")
    private val MANUAL_ISHA_TIME = stringPreferencesKey("manual_isha_time")
    private val MANUAL_JUMAH_TIME = stringPreferencesKey("manual_jumah_time")
    
    private val SILENT_MODE_ENABLED = booleanPreferencesKey("silent_mode_enabled")
    private val SILENT_MODE_FAJR_ENABLED = booleanPreferencesKey("silent_mode_fajr_enabled")
    private val SILENT_MODE_DHUHR_ENABLED = booleanPreferencesKey("silent_mode_dhuhr_enabled")
    private val SILENT_MODE_ASR_ENABLED = booleanPreferencesKey("silent_mode_asr_enabled")
    private val SILENT_MODE_MAGHRIB_ENABLED = booleanPreferencesKey("silent_mode_maghrib_enabled")
    private val SILENT_MODE_ISHA_ENABLED = booleanPreferencesKey("silent_mode_isha_enabled")
    private val SILENT_MODE_JUMAH_ENABLED = booleanPreferencesKey("silent_mode_jumah_enabled")
    private val SILENT_MODE_FAJR_DURATION = intPreferencesKey("silent_mode_fajr_duration")
    private val SILENT_MODE_DHUHR_DURATION = intPreferencesKey("silent_mode_dhuhr_duration")
    private val SILENT_MODE_ASR_DURATION = intPreferencesKey("silent_mode_asr_duration")
    private val SILENT_MODE_MAGHRIB_DURATION = intPreferencesKey("silent_mode_maghrib_duration")
    private val SILENT_MODE_ISHA_DURATION = intPreferencesKey("silent_mode_isha_duration")
    private val SILENT_MODE_JUMAH_DURATION = intPreferencesKey("silent_mode_jumah_duration")
    private val SILENT_MODE_TYPE = intPreferencesKey("silent_mode_type")
    private val SILENT_MODE_MUTE_TYPE = intPreferencesKey("silent_mode_mute_type")
    private val SILENT_MODE_AUTO_FAJR_MINUTES = intPreferencesKey("silent_mode_auto_fajr_minutes")
    private val SILENT_MODE_AUTO_DHUHR_MINUTES = intPreferencesKey("silent_mode_auto_dhuhr_minutes")
    private val SILENT_MODE_AUTO_ASR_MINUTES = intPreferencesKey("silent_mode_auto_asr_minutes")
    private val SILENT_MODE_AUTO_MAGHRIB_MINUTES = intPreferencesKey("silent_mode_auto_maghrib_minutes")
    private val SILENT_MODE_AUTO_ISHA_MINUTES = intPreferencesKey("silent_mode_auto_isha_minutes")
    private val SILENT_MODE_AUTO_JUMAH_MINUTES = intPreferencesKey("silent_mode_auto_jumah_minutes")
    private val SILENT_MODE_MANUAL_FAJR_TIME = stringPreferencesKey("silent_mode_manual_fajr_time")
    private val SILENT_MODE_MANUAL_DHUHR_TIME = stringPreferencesKey("silent_mode_manual_dhuhr_time")
    private val SILENT_MODE_MANUAL_ASR_TIME = stringPreferencesKey("silent_mode_manual_asr_time")
    private val SILENT_MODE_MANUAL_MAGHRIB_TIME = stringPreferencesKey("silent_mode_manual_maghrib_time")
    private val SILENT_MODE_MANUAL_ISHA_TIME = stringPreferencesKey("silent_mode_manual_isha_time")
    private val SILENT_MODE_MANUAL_JUMAH_TIME = stringPreferencesKey("silent_mode_manual_jumah_time")
    private val PREVIOUS_RINGER_MODE = intPreferencesKey("previous_ringer_mode")
    private val PREVIOUS_INTERRUPTION_FILTER = intPreferencesKey("previous_interruption_filter")
    

    private val AYYAM_E_BEED_REMINDER_ENABLED = booleanPreferencesKey("ayyam_e_beed_reminder_enabled")
    private val AYYAM_E_BEED_REMINDER_TIME = stringPreferencesKey("ayyam_e_beed_reminder_time")

    private val CACHED_PRAYER_DATA = stringPreferencesKey("cached_prayer_data")
    private val SAVED_LOCATIONS = stringPreferencesKey("saved_locations")


    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .map { preferences ->
            UserSettings(
                calculationMethod = preferences[CALCULATION_METHOD] ?: 4, // Default Umm-al-Qura
                asrMadhab = preferences[ASR_MADHAB] ?: 0, // Default Shafi
                hijriOffset = preferences[HIJRI_OFFSET] ?: 0,
                tahajjudMethod = preferences[TAHAJJUD_METHOD] ?: 0, // Default Last Third
                isDarkMode = preferences[IS_DARK_MODE] ?: true, // Default to true for Black & Gold
                themeMode = preferences[THEME_MODE] ?: 0, // Default to System
                is24HourFormat = preferences[IS_24_HOUR_FORMAT] ?: false,
                isAutomaticLocation = preferences[IS_AUTOMATIC_LOCATION] ?: true,
                manualLatitude = preferences[MANUAL_LATITUDE] ?: 0.0,
                manualLongitude = preferences[MANUAL_LONGITUDE] ?: 0.0,
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: false,
                notificationFajrEnabled = preferences[NOTIFICATION_FAJR_ENABLED] ?: true,
                notificationDhuhrEnabled = preferences[NOTIFICATION_DHUHR_ENABLED] ?: true,
                notificationAsrEnabled = preferences[NOTIFICATION_ASR_ENABLED] ?: true,
                notificationMaghribEnabled = preferences[NOTIFICATION_MAGHRIB_ENABLED] ?: true,
                notificationIshaEnabled = preferences[NOTIFICATION_ISHA_ENABLED] ?: true,
                notificationJumahEnabled = preferences[NOTIFICATION_JUMAH_ENABLED] ?: true,
                notificationMode = preferences[NOTIFICATION_MODE] ?: 0,
                notificationAutoFajrMinutes = preferences[NOTIFICATION_AUTO_FAJR_MINUTES] ?: 0,
                notificationAutoDhuhrMinutes = preferences[NOTIFICATION_AUTO_DHUHR_MINUTES] ?: 0,
                notificationAutoAsrMinutes = preferences[NOTIFICATION_AUTO_ASR_MINUTES] ?: 0,
                notificationAutoMaghribMinutes = preferences[NOTIFICATION_AUTO_MAGHRIB_MINUTES] ?: 0,
                notificationAutoIshaMinutes = preferences[NOTIFICATION_AUTO_ISHA_MINUTES] ?: 0,
                notificationAutoJumahMinutes = preferences[NOTIFICATION_AUTO_JUMAH_MINUTES] ?: 0,
                notificationType = preferences[NOTIFICATION_TYPE] ?: 0,
                manualFajrTime = preferences[MANUAL_FAJR_TIME] ?: "",
                manualDhuhrTime = preferences[MANUAL_DHUHR_TIME] ?: "",
                manualAsrTime = preferences[MANUAL_ASR_TIME] ?: "",
                manualMaghribTime = preferences[MANUAL_MAGHRIB_TIME] ?: "",
                manualIshaTime = preferences[MANUAL_ISHA_TIME] ?: "",
                manualJumahTime = preferences[MANUAL_JUMAH_TIME] ?: "",
                silentModeEnabled = preferences[SILENT_MODE_ENABLED] ?: false,
                silentModeFajrEnabled = preferences[SILENT_MODE_FAJR_ENABLED] ?: true,
                silentModeDhuhrEnabled = preferences[SILENT_MODE_DHUHR_ENABLED] ?: true,
                silentModeAsrEnabled = preferences[SILENT_MODE_ASR_ENABLED] ?: true,
                silentModeMaghribEnabled = preferences[SILENT_MODE_MAGHRIB_ENABLED] ?: true,
                silentModeIshaEnabled = preferences[SILENT_MODE_ISHA_ENABLED] ?: true,
                silentModeJumahEnabled = preferences[SILENT_MODE_JUMAH_ENABLED] ?: true,
                silentModeFajrDuration = preferences[SILENT_MODE_FAJR_DURATION] ?: 10,
                silentModeDhuhrDuration = preferences[SILENT_MODE_DHUHR_DURATION] ?: 10,
                silentModeAsrDuration = preferences[SILENT_MODE_ASR_DURATION] ?: 10,
                silentModeMaghribDuration = preferences[SILENT_MODE_MAGHRIB_DURATION] ?: 10,
                silentModeIshaDuration = preferences[SILENT_MODE_ISHA_DURATION] ?: 10,
                silentModeJumahDuration = preferences[SILENT_MODE_JUMAH_DURATION] ?: 10,
                silentModeType = preferences[SILENT_MODE_TYPE] ?: 0,
                silentModeMuteType = preferences[SILENT_MODE_MUTE_TYPE] ?: 0,
                silentModeAutoFajrMinutes = preferences[SILENT_MODE_AUTO_FAJR_MINUTES] ?: 0,
                silentModeAutoDhuhrMinutes = preferences[SILENT_MODE_AUTO_DHUHR_MINUTES] ?: 0,
                silentModeAutoAsrMinutes = preferences[SILENT_MODE_AUTO_ASR_MINUTES] ?: 0,
                silentModeAutoMaghribMinutes = preferences[SILENT_MODE_AUTO_MAGHRIB_MINUTES] ?: 0,
                silentModeAutoIshaMinutes = preferences[SILENT_MODE_AUTO_ISHA_MINUTES] ?: 0,
                silentModeAutoJumahMinutes = preferences[SILENT_MODE_AUTO_JUMAH_MINUTES] ?: 0,
                silentModeManualFajrTime = preferences[SILENT_MODE_MANUAL_FAJR_TIME] ?: "",
                silentModeManualDhuhrTime = preferences[SILENT_MODE_MANUAL_DHUHR_TIME] ?: "",
                silentModeManualAsrTime = preferences[SILENT_MODE_MANUAL_ASR_TIME] ?: "",
                silentModeManualMaghribTime = preferences[SILENT_MODE_MANUAL_MAGHRIB_TIME] ?: "",
                silentModeManualIshaTime = preferences[SILENT_MODE_MANUAL_ISHA_TIME] ?: "",
                silentModeManualJumahTime = preferences[SILENT_MODE_MANUAL_JUMAH_TIME] ?: "",
                previousRingerMode = preferences[PREVIOUS_RINGER_MODE] ?: -1,
                previousInterruptionFilter = preferences[PREVIOUS_INTERRUPTION_FILTER] ?: -1,
                ayyamEBeedReminderEnabled = preferences[AYYAM_E_BEED_REMINDER_ENABLED] ?: false,
                ayyamEBeedReminderTime = preferences[AYYAM_E_BEED_REMINDER_TIME] ?: "20:00"
            )
        }


    suspend fun updateAyyamEBeedReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AYYAM_E_BEED_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun updateAyyamEBeedReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[AYYAM_E_BEED_REMINDER_TIME] = time
        }
    }

    suspend fun updateCalculationMethod(method: Int) {
        context.dataStore.edit { preferences ->
            preferences[CALCULATION_METHOD] = method
        }
    }

    suspend fun updateAsrMadhab(madhab: Int) {
        context.dataStore.edit { preferences ->
            preferences[ASR_MADHAB] = madhab
        }
    }

    suspend fun updateHijriOffset(offset: Int) {
        context.dataStore.edit { preferences ->
            preferences[HIJRI_OFFSET] = offset
        }
    }

    suspend fun updateTahajjudMethod(method: Int) {
        context.dataStore.edit { preferences ->
            preferences[TAHAJJUD_METHOD] = method
        }
    }

    suspend fun updateIsDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    suspend fun updateThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun updateIs24HourFormat(is24Hour: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_24_HOUR_FORMAT] = is24Hour
        }
        WidgetUpdateScheduler.updateAllWidgets(context)
    }

    suspend fun updateIsAutomaticLocation(isAutomatic: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_AUTOMATIC_LOCATION] = isAutomatic
        }
    }

    suspend fun updateManualLocation(latitude: Double, longitude: Double) {
        context.dataStore.edit { preferences ->
            preferences[MANUAL_LATITUDE] = latitude
            preferences[MANUAL_LONGITUDE] = longitude
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateNotificationMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_MODE] = mode
        }
    }

    suspend fun updateNotificationAutoTimes(fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int, jumah: Int) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_AUTO_FAJR_MINUTES] = fajr
            preferences[NOTIFICATION_AUTO_DHUHR_MINUTES] = dhuhr
            preferences[NOTIFICATION_AUTO_ASR_MINUTES] = asr
            preferences[NOTIFICATION_AUTO_MAGHRIB_MINUTES] = maghrib
            preferences[NOTIFICATION_AUTO_ISHA_MINUTES] = isha
            preferences[NOTIFICATION_AUTO_JUMAH_MINUTES] = jumah
        }
    }

    suspend fun updateNotificationType(type: Int) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_TYPE] = type
        }
    }

    suspend fun updateManualPrayerTimes(
        fajr: String,
        dhuhr: String,
        asr: String,
        maghrib: String,
        isha: String,
        jumah: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[MANUAL_FAJR_TIME] = fajr
            preferences[MANUAL_DHUHR_TIME] = dhuhr
            preferences[MANUAL_ASR_TIME] = asr
            preferences[MANUAL_MAGHRIB_TIME] = maghrib
            preferences[MANUAL_ISHA_TIME] = isha
            preferences[MANUAL_JUMAH_TIME] = jumah
        }
    }

    suspend fun updateSilentModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SILENT_MODE_ENABLED] = enabled
        }
    }

    suspend fun updateSilentModeDurations(fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int, jumah: Int) {
        context.dataStore.edit { preferences ->
            preferences[SILENT_MODE_FAJR_DURATION] = fajr
            preferences[SILENT_MODE_DHUHR_DURATION] = dhuhr
            preferences[SILENT_MODE_ASR_DURATION] = asr
            preferences[SILENT_MODE_MAGHRIB_DURATION] = maghrib
            preferences[SILENT_MODE_ISHA_DURATION] = isha
            preferences[SILENT_MODE_JUMAH_DURATION] = jumah
        }
    }

    suspend fun updateSilentModeType(type: Int) {
        context.dataStore.edit { preferences ->
            preferences[SILENT_MODE_TYPE] = type
        }
    }

    suspend fun updateSilentModeMuteType(type: Int) {
        context.dataStore.edit { preferences ->
            preferences[SILENT_MODE_MUTE_TYPE] = type
        }
    }

    suspend fun updateSilentModeAutoTimes(
        fajr: Int,
        dhuhr: Int,
        asr: Int,
        maghrib: Int,
        isha: Int,
        jumah: Int
    ) {
        context.dataStore.edit { preferences ->
            preferences[SILENT_MODE_AUTO_FAJR_MINUTES] = fajr
            preferences[SILENT_MODE_AUTO_DHUHR_MINUTES] = dhuhr
            preferences[SILENT_MODE_AUTO_ASR_MINUTES] = asr
            preferences[SILENT_MODE_AUTO_MAGHRIB_MINUTES] = maghrib
            preferences[SILENT_MODE_AUTO_ISHA_MINUTES] = isha
            preferences[SILENT_MODE_AUTO_JUMAH_MINUTES] = jumah
        }
    }

    suspend fun updateSilentModeManualTimes(
        fajr: String,
        dhuhr: String,
        asr: String,
        maghrib: String,
        isha: String,
        jumah: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[SILENT_MODE_MANUAL_FAJR_TIME] = fajr
            preferences[SILENT_MODE_MANUAL_DHUHR_TIME] = dhuhr
            preferences[SILENT_MODE_MANUAL_ASR_TIME] = asr
            preferences[SILENT_MODE_MANUAL_MAGHRIB_TIME] = maghrib
            preferences[SILENT_MODE_MANUAL_ISHA_TIME] = isha
            preferences[SILENT_MODE_MANUAL_JUMAH_TIME] = jumah
        }
    }
    
    suspend fun updateNotificationEnabledForSalat(salatIndex: Int, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            when (salatIndex) {
                0 -> preferences[NOTIFICATION_FAJR_ENABLED] = enabled
                1 -> preferences[NOTIFICATION_DHUHR_ENABLED] = enabled
                2 -> preferences[NOTIFICATION_ASR_ENABLED] = enabled
                3 -> preferences[NOTIFICATION_MAGHRIB_ENABLED] = enabled
                4 -> preferences[NOTIFICATION_ISHA_ENABLED] = enabled
                5 -> preferences[NOTIFICATION_JUMAH_ENABLED] = enabled
            }
        }
    }
    
    suspend fun updateSilentModeEnabledForSalat(salatIndex: Int, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            when (salatIndex) {
                0 -> preferences[SILENT_MODE_FAJR_ENABLED] = enabled
                1 -> preferences[SILENT_MODE_DHUHR_ENABLED] = enabled
                2 -> preferences[SILENT_MODE_ASR_ENABLED] = enabled
                3 -> preferences[SILENT_MODE_MAGHRIB_ENABLED] = enabled
                4 -> preferences[SILENT_MODE_ISHA_ENABLED] = enabled
                5 -> preferences[SILENT_MODE_JUMAH_ENABLED] = enabled
            }
        }
    }

    suspend fun updatePreviousRingerMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[PREVIOUS_RINGER_MODE] = mode
        }
    }

    suspend fun updatePreviousInterruptionFilter(filter: Int) {
        context.dataStore.edit { preferences ->
            preferences[PREVIOUS_INTERRUPTION_FILTER] = filter
        }
    }

    val cachedPrayerDataFlow: Flow<PrayerData?> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[CACHED_PRAYER_DATA]
            if (jsonString != null) {
                try {
                    Json.decodeFromString<PrayerData>(jsonString)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }

    suspend fun updateCachedPrayerData(data: PrayerData) {
        context.dataStore.edit { preferences ->
            try {
                preferences[CACHED_PRAYER_DATA] = Json.encodeToString(data)
            } catch (e: Exception) {
                // Ignore serialization error
            }
        }
        WidgetUpdateScheduler.updateAllWidgets(context)
    }

    val savedLocationsFlow: Flow<List<SavedLocation>> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[SAVED_LOCATIONS]
            if (jsonString != null) {
                try {
                    Json.decodeFromString<List<SavedLocation>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }

    suspend fun addSavedLocation(location: SavedLocation) {
        context.dataStore.edit { preferences ->
            val jsonString = preferences[SAVED_LOCATIONS]
            val currentList = if (jsonString != null) {
                try {
                    Json.decodeFromString<List<SavedLocation>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
            // Only add if not already present by ID or name
            if (currentList.none { it.name == location.name || it.id == location.id }) {
                val newList = currentList + location
                preferences[SAVED_LOCATIONS] = Json.encodeToString(newList)
            }
        }
    }

    suspend fun removeSavedLocation(locationId: String) {
        context.dataStore.edit { preferences ->
            val jsonString = preferences[SAVED_LOCATIONS]
            val currentList = if (jsonString != null) {
                try {
                    Json.decodeFromString<List<SavedLocation>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
            val newList = currentList.filter { it.id != locationId }
            preferences[SAVED_LOCATIONS] = Json.encodeToString(newList)
        }
    }
}
