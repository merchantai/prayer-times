package com.valueappsolutions.prayertimes

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.app.NotificationManager
import android.app.AlarmManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.valueappsolutions.prayertimes.data.local.UserPreferencesRepository
import com.valueappsolutions.prayertimes.data.local.UserSettings
import com.valueappsolutions.prayertimes.domain.PrayerRepository
import com.valueappsolutions.prayertimes.ui.screens.DashboardScreen
import com.valueappsolutions.prayertimes.ui.screens.GeneralSettingsScreen
import com.valueappsolutions.prayertimes.ui.screens.HijriSettingsScreen
import com.valueappsolutions.prayertimes.ui.screens.LocationSettingsScreen
import com.valueappsolutions.prayertimes.ui.screens.NotificationSettingsScreen
import com.valueappsolutions.prayertimes.ui.screens.PrayerTimeSettingsScreen
import com.valueappsolutions.prayertimes.ui.screens.SettingsScreen
import com.valueappsolutions.prayertimes.ui.theme.PrayerTimesTheme
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerViewModel
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerViewModelFactory
import com.valueappsolutions.prayertimes.ui.screens.MainScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var prayerViewModel: PrayerViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val prayerRepository = PrayerRepository(applicationContext)

        setContent {
            val settings by preferencesRepository.userSettingsFlow.collectAsState(initial = UserSettings())
            val systemTheme = isSystemInDarkTheme()
            val isDarkTheme = when (settings.themeMode) {
                1 -> false
                2 -> true
                else -> systemTheme
            }

            PrayerTimesTheme(darkTheme = isDarkTheme) {
                val windowSizeClass = calculateWindowSizeClass(this@MainActivity)
                val widthSizeClass = windowSizeClass.widthSizeClass
                val navController = rememberNavController()
                
                val factory = PrayerViewModelFactory(prayerRepository, preferencesRepository)
                prayerViewModel = viewModel(factory = factory)
                
                val uiState by prayerViewModel.uiState.collectAsState()
                
                var hasRequestedPermission by remember { mutableStateOf(false) }

                val locationPermissionRequest = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    when {
                        permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                        permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                            fetchLocationAndUpdate()
                        }
                        else -> {
                            Toast.makeText(this, "Location permission denied. Using default location.", Toast.LENGTH_LONG).show()
                            prayerViewModel.updateLocation(21.4225, 39.8262) // Default to Makkah
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    if (!hasRequestedPermission) {
                        hasRequestedPermission = true
                        val initialSettings = preferencesRepository.userSettingsFlow.first()
                        if (initialSettings.isAutomaticLocation) {
                            if (checkPermissions()) {
                                fetchLocationAndUpdate()
                            } else {
                                locationPermissionRequest.launch(arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ))
                            }
                        } else {
                            prayerViewModel.updateLocation(initialSettings.manualLatitude, initialSettings.manualLongitude)
                        }
                    }
                }

                val alarmHelper = remember { com.valueappsolutions.prayertimes.domain.alarms.AlarmManagerHelper(this@MainActivity) }
                LaunchedEffect(uiState, settings) {
                    val prayerData = (uiState as? com.valueappsolutions.prayertimes.ui.viewmodel.PrayerUiState.Success)?.prayerData
                    alarmHelper.updateAlarms(prayerData, settings)
                }

                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                            val alarmManager = getSystemService(android.content.Context.ALARM_SERVICE) as AlarmManager
                            val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager
                            
                            var permissionsMissing = false
                            
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                permissionsMissing = true
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && androidx.core.content.ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                permissionsMissing = true
                            }
                            if (Build.VERSION.SDK_INT >= 34 && !notificationManager.canUseFullScreenIntent()) {
                                permissionsMissing = true
                            }
                            
                            if (permissionsMissing) {
                                if (settings.notificationsEnabled) {
                                    prayerViewModel.updateNotificationsEnabled(false)
                                    Toast.makeText(this@MainActivity, "Notifications disabled due to missing permissions", Toast.LENGTH_LONG).show()
                                }
                                if (settings.silentModeEnabled) {
                                    prayerViewModel.updateSilentModeEnabled(false)
                                    Toast.makeText(this@MainActivity, "Silent Mode disabled due to missing permissions", Toast.LENGTH_LONG).show()
                                }
                                if (settings.ayyamEBeedReminderEnabled) {
                                    prayerViewModel.updateAyyamEBeedReminderEnabled(false)
                                    Toast.makeText(this@MainActivity, "Ayyam-e-Beed Reminder disabled due to missing permissions", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                MainScreen(
                    navController = navController,
                    prayerViewModel = prayerViewModel,
                    uiState = uiState,
                    settings = settings,
                    isDarkTheme = isDarkTheme,
                    widthSizeClass = widthSizeClass,
                    onToggleTheme = {
                        val newMode = if (isDarkTheme) 1 else 2
                        prayerViewModel.updateThemeMode(newMode)
                    },
                    onRequestLocation = {
                        if (settings.isAutomaticLocation) {
                            if (checkPermissions()) {
                                fetchLocationAndUpdate()
                            } else {
                                locationPermissionRequest.launch(arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ))
                            }
                        }
                    },
                    locationPermissionRequest = locationPermissionRequest,
                    checkPermissions = { checkPermissions() },
                    fetchLocationAndUpdate = { fetchLocationAndUpdate() }
                )
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationAndUpdate() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                prayerViewModel.updateLocation(location.latitude, location.longitude)
            } else {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
                    .addOnSuccessListener { freshLocation ->
                        if (freshLocation != null) {
                            prayerViewModel.updateLocation(freshLocation.latitude, freshLocation.longitude)
                        } else {
                            prayerViewModel.updateLocation(21.4225, 39.8262)
                        }
                    }.addOnFailureListener {
                        prayerViewModel.updateLocation(21.4225, 39.8262)
                    }
            }
        }.addOnFailureListener {
            prayerViewModel.updateLocation(21.4225, 39.8262)
        }
    }


}
