package com.valueappsolutions.prayertimes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valueappsolutions.prayertimes.data.local.UserSettings
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerUiState
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(
    viewModel: PrayerViewModel,
    settings: UserSettings,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var themeMode by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(uiState) {
        if (uiState is PrayerUiState.Success) {
            val state = uiState as PrayerUiState.Success
            themeMode = state.themeMode
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("General", style = MaterialTheme.typography.titleLarge) },
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
                SettingsCard(title = "Theme") {
                    ThemeModeSelector(
                        themeMode = themeMode,
                        onModeSelected = {
                            themeMode = it
                            viewModel.updateThemeMode(it)
                        }
                    )
                }
            }
            
            item {
                SettingsCard(title = "Time Format") {
                    TimeFormatSelector(
                        is24HourFormat = settings.is24HourFormat,
                        onFormatSelected = {
                            viewModel.updateIs24HourFormat(it)
                        }
                    )
                }
            }
        }
        }
    }
}

@Composable
fun ThemeModeSelector(themeMode: Int, onModeSelected: (Int) -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onModeSelected(0) }
                .padding(vertical = 12.dp)
        ) {
            RadioButton(
                selected = themeMode == 0,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
            Text("System Default", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 8.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onModeSelected(1) }
                .padding(vertical = 12.dp)
        ) {
            RadioButton(
                selected = themeMode == 1,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
            Text("Light", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 8.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onModeSelected(2) }
                .padding(vertical = 12.dp)
        ) {
            RadioButton(
                selected = themeMode == 2,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
            Text("Dark", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun TimeFormatSelector(is24HourFormat: Boolean, onFormatSelected: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFormatSelected(false) }
            .padding(vertical = 12.dp)
    ) {
        RadioButton(
            selected = !is24HourFormat,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        )
        Text("12-Hour Format", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 8.dp))
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFormatSelected(true) }
            .padding(vertical = 12.dp)
    ) {
        RadioButton(
            selected = is24HourFormat,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        )
        Text("24-Hour Format", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 8.dp))
    }
}
