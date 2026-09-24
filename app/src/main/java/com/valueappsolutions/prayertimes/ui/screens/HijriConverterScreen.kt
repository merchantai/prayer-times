package com.valueappsolutions.prayertimes.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijriConverterScreen(hijriOffset: Int = 0) {
    val scrollState = rememberScrollState()
    
    // Today's dates
    val todayGregorian = remember { LocalDate.now() }
    val todayHijri = remember(hijriOffset) { HijrahDate.now().plus(hijriOffset.toLong(), java.time.temporal.ChronoUnit.DAYS) }
    
    // Gregorian -> Hijri state
    var showGregorianPicker by remember { mutableStateOf(false) }
    var selectedGregorianDate by remember { mutableStateOf(todayGregorian) }
    val convertedHijriDate = remember(selectedGregorianDate, hijriOffset) { 
        HijrahDate.from(selectedGregorianDate).plus(hijriOffset.toLong(), java.time.temporal.ChronoUnit.DAYS) 
    }
    val gregorianDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedGregorianDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    // Hijri -> Gregorian state
    var selectedHijriDay by remember(todayHijri) { androidx.compose.runtime.mutableIntStateOf(todayHijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)) }
    var selectedHijriMonth by remember(todayHijri) { androidx.compose.runtime.mutableIntStateOf(todayHijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)) }
    var selectedHijriYear by remember(todayHijri) { androidx.compose.runtime.mutableIntStateOf(todayHijri.get(java.time.temporal.ChronoField.YEAR)) }
    
    val convertedGregorianDate = remember(selectedHijriDay, selectedHijriMonth, selectedHijriYear, hijriOffset) {
        try {
            val hijri = HijrahDate.of(selectedHijriYear, selectedHijriMonth, selectedHijriDay)
            LocalDate.from(hijri).minusDays(hijriOffset.toLong())
        } catch (e: Exception) {
            null // Handle invalid dates like 30th of a 29-day month
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Hijri Calendar", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        
        // 1. Today's Date Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Today's Date", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = todayHijri.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = todayGregorian.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // 2. Gregorian to Hijri Converter
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Gregorian to Hijri", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { showGregorianPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedGregorianDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hijri Date", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = convertedHijriDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 3. Hijri to Gregorian Converter
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Hijri to Gregorian", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val hijriMonths = listOf(
                        "1 - Muharram", "2 - Safar", "3 - Rabi' I", "4 - Rabi' II",
                        "5 - Jumada I", "6 - Jumada II", "7 - Rajab", "8 - Sha'ban",
                        "9 - Ramadan", "10 - Shawwal", "11 - Dhu al-Qi'dah", "12 - Dhu al-Hijjah"
                    )
                    // Day Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        DropdownMenuBox("Day", selectedHijriDay, 1..30) { selectedHijriDay = it }
                    }
                    // Month Dropdown
                    Box(modifier = Modifier.weight(1.5f)) {
                        DropdownMenuBox(
                            label = "Month", 
                            selectedValue = selectedHijriMonth, 
                            range = 1..12,
                            valueFormatter = { hijriMonths[it - 1] }
                        ) { selectedHijriMonth = it }
                    }
                    // Year Dropdown
                    Box(modifier = Modifier.weight(1.2f)) {
                        DropdownMenuBox("Year", selectedHijriYear, 1300..1500) { selectedHijriYear = it }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Gregorian Date", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (convertedGregorianDate != null) {
                            Text(
                                text = convertedGregorianDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Invalid Date",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            }
        }
    }
    if (showGregorianPicker) {
        DatePickerDialog(
            onDismissRequest = { showGregorianPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    gregorianDatePickerState.selectedDateMillis?.let {
                        selectedGregorianDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showGregorianPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGregorianPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = gregorianDatePickerState)
        }
    }
    }
}

@Composable
private fun DropdownMenuBox(
    label: String,
    selectedValue: Int,
    range: IntRange,
    valueFormatter: (Int) -> String = { it.toString() },
    onValueSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(valueFormatter(selectedValue), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand", tint = MaterialTheme.colorScheme.primary)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 240.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            range.forEach { value ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = valueFormatter(value), 
                            color = if (value == selectedValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (value == selectedValue) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    onClick = {
                        onValueSelected(value)
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (value == selectedValue) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                        else Color.Transparent
                    )
                )
            }
        }
    }
}
