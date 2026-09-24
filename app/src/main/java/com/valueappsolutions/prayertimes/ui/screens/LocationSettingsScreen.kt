package com.valueappsolutions.prayertimes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.valueappsolutions.prayertimes.data.local.UserSettings
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSettingsScreen(
    viewModel: PrayerViewModel,
    settings: UserSettings,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Location", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
            item {
                SettingsCard(title = "Location Mode") {
                    LocationModeSelector(
                        isAutomatic = settings.isAutomaticLocation,
                        manualLatitude = settings.manualLatitude,
                        manualLongitude = settings.manualLongitude,
                        onModeSelected = { isAutomatic ->
                            viewModel.updateIsAutomaticLocation(isAutomatic)
                        },
                        onManualLocationSaved = { lat, lng ->
                            viewModel.updateManualLocation(lat, lng)
                        }
                    )
                }
            }
        }
        }
    }
}

@Composable
fun LocationModeSelector(
    isAutomatic: Boolean,
    manualLatitude: Double,
    manualLongitude: Double,
    onModeSelected: (Boolean) -> Unit,
    onManualLocationSaved: (Double, Double) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onModeSelected(true) }
                .padding(vertical = 12.dp)
        ) {
            RadioButton(
                selected = isAutomatic,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
            Text("Automatic Location", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 8.dp))
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    showDialog = true
                }
                .padding(vertical = 12.dp)
        ) {
            RadioButton(
                selected = !isAutomatic,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text("Manual Location", color = MaterialTheme.colorScheme.onBackground)
                if (!isAutomatic) {
                    Text(
                        text = "Lat: $manualLatitude, Lng: $manualLongitude",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    if (showDialog) {
        ManualLocationDialog(
            initialLatitude = manualLatitude,
            initialLongitude = manualLongitude,
            onDismiss = { showDialog = false },
            onSave = { lat, lng ->
                onManualLocationSaved(lat, lng)
                onModeSelected(false)
                showDialog = false
            }
        )
    }
}

@Composable
fun ManualLocationDialog(
    initialLatitude: Double,
    initialLongitude: Double,
    onDismiss: () -> Unit,
    onSave: (Double, Double) -> Unit
) {
    var latText by remember { mutableStateOf(if (initialLatitude != 0.0) initialLatitude.toString() else "") }
    var lngText by remember { mutableStateOf(if (initialLongitude != 0.0) initialLongitude.toString() else "") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Location", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                OutlinedTextField(
                    value = latText,
                    onValueChange = { 
                        latText = it
                        isError = false
                    },
                    label = { Text("Latitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lngText,
                    onValueChange = { 
                        lngText = it
                        isError = false
                    },
                    label = { Text("Longitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                if (isError) {
                    Text(
                        text = "Please enter valid coordinates",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lng = lngText.toDoubleOrNull()
                    if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                        onSave(lat, lng)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Save", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}
