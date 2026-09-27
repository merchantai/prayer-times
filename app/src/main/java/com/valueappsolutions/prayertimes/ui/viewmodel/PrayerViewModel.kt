package com.valueappsolutions.prayertimes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import com.valueappsolutions.prayertimes.data.local.UserPreferencesRepository
import com.valueappsolutions.prayertimes.domain.PrayerData
import com.valueappsolutions.prayertimes.domain.PrayerRepository
import com.valueappsolutions.prayertimes.domain.SavedLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import com.valueappsolutions.prayertimes.data.local.UserSettings

data class LocationRequest(
    val latitude: Double,
    val longitude: Double,
    val isAutomatic: Boolean? = null,
    val timezoneId: String? = null,
    val cityName: String? = null
)

sealed class PrayerUiState {
    object Loading : PrayerUiState()
    data class Success(
        val prayerData: PrayerData,
        val isRefreshing: Boolean = false,
        val method: Int,
        val asrMadhab: Int,
        val hijriOffset: Int,
        val tahajjudMethod: Int,
        val themeMode: Int
    ) : PrayerUiState()
    data class Error(val message: String) : PrayerUiState()
}

class PrayerViewModel(
    private val repository: PrayerRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PrayerUiState>(PrayerUiState.Loading)
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    val userSettings: StateFlow<UserSettings> = preferencesRepository.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    val savedLocations: StateFlow<List<SavedLocation>> = preferencesRepository.savedLocationsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    private val _locationFlow = MutableStateFlow<LocationRequest?>(null)
    val currentLocation: StateFlow<LocationRequest?> = _locationFlow.asStateFlow()

    init {
        viewModelScope.launch {
            val calcParamsFlow = preferencesRepository.userSettingsFlow.map { 
                listOf(it.calculationMethod, it.asrMadhab, it.hijriOffset, it.tahajjudMethod) 
            }.distinctUntilChanged()
            
            kotlinx.coroutines.flow.combine(
                _locationFlow,
                calcParamsFlow
            ) { location, params ->
                if (location != null) {
                    val method = params[0]
                    val madhab = params[1]
                    val offset = params[2]
                    val tahajjud = params[3]
                    
                    fetchPrayerTimes(
                        location.latitude, location.longitude,
                        method, madhab, offset, tahajjud,
                        isAutomatic = location.isAutomatic,
                        timezoneId = location.timezoneId,
                        cityName = location.cityName
                    )
                }
            }.collectLatest { }
        }
        
        viewModelScope.launch {
            preferencesRepository.userSettingsFlow.map { it.themeMode }.distinctUntilChanged().collectLatest { theme ->
                val currentState = _uiState.value
                if (currentState is PrayerUiState.Success) {
                    _uiState.update { currentState.copy(themeMode = theme) }
                }
            }
        }
    }

    fun updateLocation(latitude: Double, longitude: Double, isAutomatic: Boolean? = null, timezoneId: String? = null, cityName: String? = null) {
        _locationFlow.value = LocationRequest(latitude, longitude, isAutomatic, timezoneId, cityName)
    }

    fun refreshData() {
        viewModelScope.launch {
            val location = _locationFlow.value ?: run {
                val cached = preferencesRepository.cachedPrayerDataFlow.first()
                if (cached != null) LocationRequest(cached.latitude, cached.longitude) else null
            } ?: return@launch
            
            val settings = preferencesRepository.userSettingsFlow.first()
            fetchPrayerTimes(
                location.latitude, location.longitude,
                settings.calculationMethod, settings.asrMadhab, settings.hijriOffset,
                settings.tahajjudMethod,
                forceRefresh = true
            )
        }
    }

    private suspend fun fetchPrayerTimes(
        lat: Double, lng: Double,
        method: Int, madhab: Int, offset: Int, tahajjudMethod: Int,
        forceRefresh: Boolean = false,
        isAutomatic: Boolean? = null,
        timezoneId: String? = null,
        cityName: String? = null
    ) {
        val cachedData = preferencesRepository.cachedPrayerDataFlow.first()
        val settings = preferencesRepository.userSettingsFlow.first()
        val currentDateStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US).format(java.util.Date())

        if (cachedData != null) {
            _uiState.update { 
                PrayerUiState.Success(cachedData, true, method, madhab, offset, tahajjudMethod, settings.themeMode) 
            }
        } else {
            _uiState.update { PrayerUiState.Loading }
        }

        try {
            val startTime = System.currentTimeMillis()
            
            val isAuto = isAutomatic ?: settings.isAutomaticLocation
            val tz = if (isAuto) java.time.ZoneId.systemDefault().id else (timezoneId ?: cachedData?.destinationTimezoneId)
            val finalCityName = cityName ?: (if (cachedData != null && calculateDistanceInKm(cachedData.latitude, cachedData.longitude, lat, lng) < 5.0) cachedData.cityName else null)
            val finalAreaName = if (cityName == null && cachedData != null && calculateDistanceInKm(cachedData.latitude, cachedData.longitude, lat, lng) < 5.0) cachedData.areaName else null

            val data = repository.getPrayerTimes(
                lat, lng, method, madhab, offset, tahajjudMethod,
                providedArea = finalAreaName,
                providedCity = finalCityName,
                savedTimezoneId = tz
            )
            
            val elapsedTime = System.currentTimeMillis() - startTime
            if (forceRefresh && elapsedTime < 500) {
                kotlinx.coroutines.delay(500 - elapsedTime)
            }
            
            val updatedData = data.copy(lastUpdatedTime = System.currentTimeMillis(), latitude = lat, longitude = lng)
            preferencesRepository.updateCachedPrayerData(updatedData)
            _uiState.update { PrayerUiState.Success(updatedData, false, method, madhab, offset, tahajjudMethod, settings.themeMode) }

            if (updatedData.areaName.isEmpty() && updatedData.cityName.isEmpty()) {
                viewModelScope.launch {
                    val (area, city) = repository.resolveLocationName(lat, lng)
                    if (area.isNotEmpty() || city.isNotEmpty()) {
                        val finalData = updatedData.copy(areaName = area, cityName = city)
                        preferencesRepository.updateCachedPrayerData(finalData)
                        _uiState.update { state -> 
                            if (state is PrayerUiState.Success) {
                                state.copy(prayerData = finalData)
                            } else state
                        }
                    }
                }
            }
        } catch (e: Exception) {
            if (cachedData == null) {
                _uiState.update { PrayerUiState.Error(e.message ?: "Unknown error") }
            } else {
                _uiState.update { PrayerUiState.Success(cachedData, false, method, madhab, offset, tahajjudMethod, settings.themeMode) }
            }
        }
    }

    fun updateMethod(method: Int) {
        viewModelScope.launch {
            preferencesRepository.updateCalculationMethod(method)
        }
    }


    fun updateAyyamEBeedReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAyyamEBeedReminderEnabled(enabled)
        }
    }

    fun updateAyyamEBeedReminderTime(time: String) {
        viewModelScope.launch {
            preferencesRepository.updateAyyamEBeedReminderTime(time)
        }
    }

    fun updateMadhab(madhab: Int) {
        viewModelScope.launch {
            preferencesRepository.updateAsrMadhab(madhab)
        }
    }

    fun updateHijriOffset(offset: Int) {
        viewModelScope.launch {
            preferencesRepository.updateHijriOffset(offset)
        }
    }

    fun updateTahajjudMethod(method: Int) {
        viewModelScope.launch {
            preferencesRepository.updateTahajjudMethod(method)
        }
    }

    fun updateIsDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateIsDarkMode(isDark)
        }
    }

    fun updateThemeMode(mode: Int) {
        viewModelScope.launch {
            preferencesRepository.updateThemeMode(mode)
        }
    }

    fun updateIs24HourFormat(is24Hour: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateIs24HourFormat(is24Hour)
        }
    }

    fun updateIsAutomaticLocation(isAutomatic: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateIsAutomaticLocation(isAutomatic)
        }
    }

    fun updateManualLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            preferencesRepository.updateManualLocation(latitude, longitude)
            updateLocation(latitude, longitude)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationsEnabled(enabled)
        }
    }

    fun updateNotificationMode(mode: Int) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationMode(mode)
        }
    }

    fun updateNotificationAutoTimes(fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int, jumah: Int) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationAutoTimes(fajr, dhuhr, asr, maghrib, isha, jumah)
        }
    }

    fun updateNotificationType(type: Int) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationType(type)
        }
    }

    fun updateManualPrayerTimes(
        fajr: String,
        dhuhr: String,
        asr: String,
        maghrib: String,
        isha: String,
        jumah: String
    ) {
        viewModelScope.launch {
            preferencesRepository.updateManualPrayerTimes(fajr, dhuhr, asr, maghrib, isha, jumah)
        }
    }

    fun updateSilentModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateSilentModeEnabled(enabled)
        }
    }

    fun updateSilentModeDurations(fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int, jumah: Int) {
        viewModelScope.launch {
            preferencesRepository.updateSilentModeDurations(fajr, dhuhr, asr, maghrib, isha, jumah)
        }
    }

    fun updateSilentModeType(type: Int) {
        viewModelScope.launch {
            preferencesRepository.updateSilentModeType(type)
        }
    }

    fun updateSilentModeMuteType(type: Int) {
        viewModelScope.launch {
            preferencesRepository.updateSilentModeMuteType(type)
        }
    }

    fun updateSilentModeAutoTimes(fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int, jumah: Int) {
        viewModelScope.launch {
            preferencesRepository.updateSilentModeAutoTimes(fajr, dhuhr, asr, maghrib, isha, jumah)
        }
    }

    fun updateSilentModeManualTimes(
        fajr: String,
        dhuhr: String,
        asr: String,
        maghrib: String,
        isha: String,
        jumah: String
    ) {
        viewModelScope.launch {
            preferencesRepository.updateSilentModeManualTimes(fajr, dhuhr, asr, maghrib, isha, jumah)
        }
    }
    
    fun updateNotificationEnabledForSalat(salatIndex: Int, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationEnabledForSalat(salatIndex, enabled)
        }
    }

    fun updateSilentModeEnabledForSalat(salatIndex: Int, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateSilentModeEnabledForSalat(salatIndex, enabled)
        }
    }

    fun saveLocation(location: SavedLocation) {
        viewModelScope.launch {
            preferencesRepository.addSavedLocation(location)
        }
    }

    fun removeLocation(locationId: String) {
        viewModelScope.launch {
            preferencesRepository.removeSavedLocation(locationId)
        }
    }

    fun setAsCurrentLocation(location: SavedLocation) {
        viewModelScope.launch {
            preferencesRepository.updateIsAutomaticLocation(false)
            preferencesRepository.updateManualLocation(location.latitude, location.longitude)
            
            // Just trigger the update, fetchPrayerTimes will handle fetching and caching
            updateLocation(location.latitude, location.longitude, isAutomatic = false, timezoneId = location.timezoneId, cityName = location.name)
        }
    }

    suspend fun getPrayerTimesForLocation(lat: Double, lng: Double, locationName: String? = null, timezoneId: String? = null): PrayerData {
        val settings = preferencesRepository.userSettingsFlow.first()
        return repository.getPrayerTimes(
            lat, lng, 
            settings.calculationMethod, 
            settings.asrMadhab, 
            settings.hijriOffset, 
            settings.tahajjudMethod,
            providedArea = "",
            providedCity = locationName,
            savedTimezoneId = timezoneId
        ).copy(latitude = lat, longitude = lng)
    }


    private fun calculateDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of earth in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return r * c
    }
}

class PrayerViewModelFactory(
    private val repository: PrayerRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrayerViewModel(repository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
