package com.valueappsolutions.prayertimes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valueappsolutions.prayertimes.domain.PrayerData
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailScreen(
    viewModel: PrayerViewModel,
    lat: Double,
    lng: Double,
    name: String,
    timezoneId: String? = null,
    is24HourFormat: Boolean,
    onNavigateBack: () -> Unit
) {
    var prayerData by remember { mutableStateOf<PrayerData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lat, lng) {
        isLoading = true
        errorMessage = null
        try {
            val data = viewModel.getPrayerTimesForLocation(lat, lng, locationName = name, timezoneId = timezoneId)
            prayerData = data.copy(cityName = name, areaName = "") // override location text
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load prayer times."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            } else if (prayerData != null) {
                DashboardContent(
                    data = prayerData!!,
                    isRefreshing = false,
                    is24HourFormat = is24HourFormat,
                    isExpanded = false, // Detail screen doesn't need to be multi-column on tablet right now
                    showRealtimeInfo = false // Hide countdown & highlight
                )
            }
        }
    }
}
