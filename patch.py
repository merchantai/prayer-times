import re

with open('/Users/merchant/apps/prayer-times/app/src/main/java/com/valueappsolutions/prayertimes/ui/viewmodel/PrayerViewModel.kt', 'r') as f:
    content = f.read()

# Add LocationRequest class
location_request_class = """data class LocationRequest(
    val latitude: Double,
    val longitude: Double,
    val isAutomatic: Boolean? = null,
    val timezoneId: String? = null,
    val cityName: String? = null
)

sealed class PrayerUiState {"""
content = content.replace("sealed class PrayerUiState {", location_request_class)

# Change _locationFlow type
content = content.replace(
    "private val _locationFlow = MutableStateFlow<Pair<Double, Double>?>(null)",
    "private val _locationFlow = MutableStateFlow<LocationRequest?>(null)"
)
content = content.replace(
    "val currentLocation: StateFlow<Pair<Double, Double>?> = _locationFlow.asStateFlow()",
    "val currentLocation: StateFlow<LocationRequest?> = _locationFlow.asStateFlow()"
)

# Update combine block
old_combine = """                    fetchPrayerTimes(
                        location.first, location.second,
                        method, madhab, offset, tahajjud
                    )"""
new_combine = """                    fetchPrayerTimes(
                        location.latitude, location.longitude,
                        method, madhab, offset, tahajjud,
                        isAutomatic = location.isAutomatic,
                        timezoneId = location.timezoneId,
                        cityName = location.cityName
                    )"""
content = content.replace(old_combine, new_combine)

# Update updateLocation method
old_updateLocation = """    fun updateLocation(latitude: Double, longitude: Double) {
        _locationFlow.value = Pair(latitude, longitude)
    }"""
new_updateLocation = """    fun updateLocation(latitude: Double, longitude: Double, isAutomatic: Boolean? = null, timezoneId: String? = null, cityName: String? = null) {
        _locationFlow.value = LocationRequest(latitude, longitude, isAutomatic, timezoneId, cityName)
    }"""
content = content.replace(old_updateLocation, new_updateLocation)

# Update refreshData method
old_refreshData = """            val location = _locationFlow.value ?: run {
                val cached = preferencesRepository.cachedPrayerDataFlow.first()
                if (cached != null) Pair(cached.latitude, cached.longitude) else null
            } ?: return@launch
            
            val settings = preferencesRepository.userSettingsFlow.first()
            fetchPrayerTimes(
                location.first, location.second,"""
new_refreshData = """            val location = _locationFlow.value ?: run {
                val cached = preferencesRepository.cachedPrayerDataFlow.first()
                if (cached != null) LocationRequest(cached.latitude, cached.longitude) else null
            } ?: return@launch
            
            val settings = preferencesRepository.userSettingsFlow.first()
            fetchPrayerTimes(
                location.latitude, location.longitude,"""
content = content.replace(old_refreshData, new_refreshData)

# Update fetchPrayerTimes signature
old_fetch_sig = """    private suspend fun fetchPrayerTimes(
        lat: Double, lng: Double,
        method: Int, madhab: Int, offset: Int, tahajjudMethod: Int,
        forceRefresh: Boolean = false
    ) {"""
new_fetch_sig = """    private suspend fun fetchPrayerTimes(
        lat: Double, lng: Double,
        method: Int, madhab: Int, offset: Int, tahajjudMethod: Int,
        forceRefresh: Boolean = false,
        isAutomatic: Boolean? = null,
        timezoneId: String? = null,
        cityName: String? = null
    ) {"""
content = content.replace(old_fetch_sig, new_fetch_sig)

# Update fetchPrayerTimes logic
old_tz_logic = """            val tz = if (settings.isAutomaticLocation) java.time.ZoneId.systemDefault().id else cachedData?.destinationTimezoneId
            val data = if (cachedData != null && calculateDistanceInKm(cachedData.latitude, cachedData.longitude, lat, lng) < 5.0) {
                repository.getPrayerTimes(
                    lat, lng, method, madhab, offset, tahajjudMethod,
                    providedArea = cachedData.areaName,
                    providedCity = cachedData.cityName,
                    savedTimezoneId = tz
                )
            } else {
                repository.getPrayerTimes(
                    lat, lng, method, madhab, offset, tahajjudMethod,
                    savedTimezoneId = tz
                )
            }"""
new_tz_logic = """            val isAuto = isAutomatic ?: settings.isAutomaticLocation
            val tz = if (isAuto) java.time.ZoneId.systemDefault().id else (timezoneId ?: cachedData?.destinationTimezoneId)
            val finalCityName = cityName ?: (if (cachedData != null && calculateDistanceInKm(cachedData.latitude, cachedData.longitude, lat, lng) < 5.0) cachedData.cityName else null)
            val finalAreaName = if (cityName == null && cachedData != null && calculateDistanceInKm(cachedData.latitude, cachedData.longitude, lat, lng) < 5.0) cachedData.areaName else null

            val data = repository.getPrayerTimes(
                lat, lng, method, madhab, offset, tahajjudMethod,
                providedArea = finalAreaName,
                providedCity = finalCityName,
                savedTimezoneId = tz
            )"""
content = content.replace(old_tz_logic, new_tz_logic)

# Update setAsCurrentLocation
old_set_as_current = """    fun setAsCurrentLocation(location: SavedLocation) {
        viewModelScope.launch {
            preferencesRepository.updateIsAutomaticLocation(false)
            preferencesRepository.updateManualLocation(location.latitude, location.longitude)
            
            val settings = preferencesRepository.userSettingsFlow.first()
            val newData = repository.getPrayerTimes(
                location.latitude, location.longitude,
                settings.calculationMethod,
                settings.asrMadhab,
                settings.hijriOffset,
                settings.tahajjudMethod,
                providedArea = "",
                providedCity = location.name,
                savedTimezoneId = location.timezoneId
            )
            preferencesRepository.updateCachedPrayerData(newData.copy(latitude = location.latitude, longitude = location.longitude))
            updateLocation(location.latitude, location.longitude)
        }
    }"""
new_set_as_current = """    fun setAsCurrentLocation(location: SavedLocation) {
        viewModelScope.launch {
            preferencesRepository.updateIsAutomaticLocation(false)
            preferencesRepository.updateManualLocation(location.latitude, location.longitude)
            
            // Just trigger the update, fetchPrayerTimes will handle fetching and caching
            updateLocation(location.latitude, location.longitude, isAutomatic = false, timezoneId = location.timezoneId, cityName = location.name)
        }
    }"""
content = content.replace(old_set_as_current, new_set_as_current)

with open('/Users/merchant/apps/prayer-times/app/src/main/java/com/valueappsolutions/prayertimes/ui/viewmodel/PrayerViewModel.kt', 'w') as f:
    f.write(content)
