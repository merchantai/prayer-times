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
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerUiState
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimeSettingsScreen(
    viewModel: PrayerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.userSettings.collectAsState()

    var currentMethod by remember { mutableIntStateOf(2) }
    var currentMadhab by remember { mutableIntStateOf(0) }
    var currentTahajjudMethod by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(uiState, settings) {
        if (uiState is PrayerUiState.Success) {
            val state = uiState as PrayerUiState.Success
            currentMethod = state.method
            currentMadhab = state.asrMadhab
            currentTahajjudMethod = state.tahajjudMethod
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prayer Times", style = MaterialTheme.typography.titleLarge) },
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
                SettingsCard(title = "Calculation Method") {
                    CalculationMethodSelector(
                        selectedMethod = currentMethod,
                        onMethodSelected = {
                            currentMethod = it
                            viewModel.updateMethod(it)
                        }
                    )
                }
            }

            item {
                SettingsCard(title = "Asr Madhab") {
                    AsrMadhabSelector(
                        selectedMadhab = currentMadhab,
                        onMadhabSelected = {
                            currentMadhab = it
                            viewModel.updateMadhab(it)
                        }
                    )
                }
            }

            item {
                SettingsCard(title = "Tahajjud Calculation Method") {
                    TahajjudMethodSelector(
                        selectedMethod = currentTahajjudMethod,
                        onMethodSelected = {
                            currentTahajjudMethod = it
                            viewModel.updateTahajjudMethod(it)
                        }
                    )
                }
            }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationMethodSelector(selectedMethod: Int, onMethodSelected: (Int) -> Unit) {
    val methods = mapOf(
        0 to "Shia Ithna-Ashari, Leva Institute, Qum",
        1 to "University of Islamic Sciences, Karachi",
        2 to "Islamic Society of North America (ISNA)",
        3 to "Muslim World League",
        4 to "Umm Al-Qura University, Makkah",
        5 to "Egyptian General Authority of Survey",
        7 to "Institute of Geophysics, University of Tehran",
        8 to "Gulf Region",
        9 to "Kuwait",
        10 to "Qatar",
        11 to "Majlis Ugama Islam Singapura, Singapore",
        12 to "Union Organization Islamic de France",
        13 to "Diyanet İşleri Başkanlığı, Turkey",
        14 to "Spiritual Administration of Muslims of Russia",
        15 to "Moonsighting Committee Worldwide",
        16 to "Dubai",
        17 to "Jabatan Kemajuan Islam Malaysia (JAKIM)",
        18 to "Tunisia",
        19 to "Algeria",
        20 to "Kementerian Agama Republik Indonesia",
        21 to "Morocco",
        22 to "Comunidade Islamica de Lisboa",
        23 to "Ministry of Awqaf, Jordan",
        99 to "Custom"
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = methods[selectedMethod] ?: "Unknown",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            methods.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onMethodSelected(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AsrMadhabSelector(selectedMadhab: Int, onMadhabSelected: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMadhabSelected(0) }
            .padding(vertical = 12.dp)
    ) {
        RadioButton(
            selected = selectedMadhab == 0,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        )
        Text("Standard (Shafi, Maliki, Hanbali)", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 8.dp))
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMadhabSelected(1) }
            .padding(vertical = 12.dp)
    ) {
        RadioButton(
            selected = selectedMadhab == 1,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        )
        Text("Hanafi", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun TahajjudMethodSelector(selectedMethod: Int, onMethodSelected: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMethodSelected(0) }
            .padding(vertical = 12.dp)
    ) {
        RadioButton(
            selected = selectedMethod == 0,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        )
        Text("Last Third of the Night", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 8.dp))
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMethodSelected(1) }
            .padding(vertical = 12.dp)
    ) {
        RadioButton(
            selected = selectedMethod == 1,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        )
        Text("Midnight (Middle of the Night)", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 8.dp))
    }
}
