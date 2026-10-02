package com.valueappsolutions.prayertimes.ui.screens

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.valueappsolutions.prayertimes.ui.components.ComposeTimePickerDialog
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilentModeSettingsScreen(
    viewModel: PrayerViewModel,
    onNavigateBack: () -> Unit
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current
    var showTimePickerFor by remember { mutableStateOf<Int?>(null) }
    
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    var pendingSalatIndex by remember { mutableStateOf<Int?>(null) }
    var pendingSalatState by remember { mutableStateOf(false) }
    
    val exactAlarmLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            pendingSalatIndex?.let { index ->
                if (index == -1) viewModel.updateSilentModeEnabled(pendingSalatState)
                else viewModel.updateSilentModeEnabledForSalat(index, pendingSalatState)
            }
        } else {
            Toast.makeText(context, "Exact Alarm permission is required for this feature", Toast.LENGTH_SHORT).show()
        }
        pendingSalatIndex = null
    }

    val dndLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager.isNotificationPolicyAccessGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "Please allow Exact Alarms for prayer notifications", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                exactAlarmLauncher.launch(intent)
            } else {
                pendingSalatIndex?.let { index ->
                    if (index == -1) viewModel.updateSilentModeEnabled(pendingSalatState)
                    else viewModel.updateSilentModeEnabledForSalat(index, pendingSalatState)
                }
                pendingSalatIndex = null
            }
        } else {
            Toast.makeText(context, "Do Not Disturb access is required for Silent Mode", Toast.LENGTH_SHORT).show()
            pendingSalatIndex = null
        }
    }

    val checkAndToggleSilentMode: (Int, Boolean) -> Unit = { index, enabled ->
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
                pendingSalatIndex = index
                pendingSalatState = enabled
                val intent = if (Build.VERSION.SDK_INT >= 30) {
                    Intent("android.settings.NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS").apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                } else {
                    Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                }
                Toast.makeText(context, "Please allow Do Not Disturb access for Silent Mode", Toast.LENGTH_LONG).show()
                try {
                    dndLauncher.launch(intent)
                } catch (e: Exception) {
                    dndLauncher.launch(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                pendingSalatIndex = index
                pendingSalatState = enabled
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                Toast.makeText(context, "Please allow Exact Alarms for prayer notifications", Toast.LENGTH_LONG).show()
                exactAlarmLauncher.launch(intent)
            } else {
                if (index == -1) viewModel.updateSilentModeEnabled(enabled)
                else viewModel.updateSilentModeEnabledForSalat(index, enabled)
            }
        } else {
            if (index == -1) viewModel.updateSilentModeEnabled(enabled)
            else viewModel.updateSilentModeEnabledForSalat(index, enabled)
        }
    }
    
    val prayers = listOf(
        "Fajr" to userSettings.silentModeManualFajrTime,
        "Dhuhr" to userSettings.silentModeManualDhuhrTime,
        "Asr" to userSettings.silentModeManualAsrTime,
        "Maghrib" to userSettings.silentModeManualMaghribTime,
        "Isha" to userSettings.silentModeManualIshaTime,
        "Jumah" to userSettings.silentModeManualJumahTime
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Silent Mode Settings", style = MaterialTheme.typography.titleLarge) },
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
                SettingsCard(title = "Master Switch") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Auto Silent Mode",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = userSettings.silentModeEnabled,
                            onCheckedChange = { checkAndToggleSilentMode(-1, it) }
                        )
                    }
                    if (!notificationManager.isNotificationPolicyAccessGranted) {
                        Text(
                            text = "Note: Requires 'Do Not Disturb' access permission to modify phone volume.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            if (userSettings.silentModeEnabled) {

                item {
                    SettingsCard(title = "Mute Type") {
                        val types = listOf("Silent (Mute Ringer)", "Do Not Disturb (DND)", "Vibrate")
                        types.forEachIndexed { index, type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateSilentModeMuteType(index) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = userSettings.silentModeMuteType == index,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = type,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                item {
                    SettingsCard(title = "Silent Mode Trigger") {
                        val modes = listOf("Automatic (Offset from Salat)", "Manual (Exact Times)")
                        modes.forEachIndexed { index, mode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateSilentModeType(index) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = userSettings.silentModeType == index,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = mode,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                if (userSettings.silentModeType == 0) {
                    item {
                        SettingsCard(title = "Automatic Settings") {
                            val autoOffsets = listOf(
                                "Fajr" to userSettings.silentModeAutoFajrMinutes,
                                "Dhuhr" to userSettings.silentModeAutoDhuhrMinutes,
                                "Asr" to userSettings.silentModeAutoAsrMinutes,
                                "Maghrib" to userSettings.silentModeAutoMaghribMinutes,
                                "Isha" to userSettings.silentModeAutoIshaMinutes,
                                "Jumah" to userSettings.silentModeAutoJumahMinutes
                            )
                            
                            val durations = listOf(
                                userSettings.silentModeFajrDuration,
                                userSettings.silentModeDhuhrDuration,
                                userSettings.silentModeAsrDuration,
                                userSettings.silentModeMaghribDuration,
                                userSettings.silentModeIshaDuration,
                                userSettings.silentModeJumahDuration
                            )
                            
                            val salatEnabled = listOf(
                                userSettings.silentModeFajrEnabled,
                                userSettings.silentModeDhuhrEnabled,
                                userSettings.silentModeAsrEnabled,
                                userSettings.silentModeMaghribEnabled,
                                userSettings.silentModeIshaEnabled,
                                userSettings.silentModeJumahEnabled
                            )
                            
                            autoOffsets.forEachIndexed { index, pair ->
                                var offsetText by remember { mutableStateOf(pair.second.toString()) }
                                LaunchedEffect(pair.second) {
                                    val currentInt = offsetText.toIntOrNull() ?: 0
                                    val newInt = pair.second.toString().toIntOrNull() ?: 0
                                    if (currentInt != newInt) {
                                        offsetText = pair.second.toString()
                                    }
                                }
                                
                                var durationText by remember { mutableStateOf(durations[index].toString()) }
                                LaunchedEffect(durations[index]) {
                                    val currentInt = durationText.toIntOrNull() ?: 10
                                    val newInt = durations[index].toString().toIntOrNull() ?: 10
                                    if (currentInt != newInt) {
                                        durationText = durations[index].toString()
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = offsetText,
                                        onValueChange = { newValue ->
                                            offsetText = newValue
                                            val minutes = newValue.toIntOrNull() ?: 0
                                            when (index) {
                                                0 -> viewModel.updateSilentModeAutoTimes(minutes, autoOffsets[1].second, autoOffsets[2].second, autoOffsets[3].second, autoOffsets[4].second, autoOffsets[5].second)
                                                1 -> viewModel.updateSilentModeAutoTimes(autoOffsets[0].second, minutes, autoOffsets[2].second, autoOffsets[3].second, autoOffsets[4].second, autoOffsets[5].second)
                                                2 -> viewModel.updateSilentModeAutoTimes(autoOffsets[0].second, autoOffsets[1].second, minutes, autoOffsets[3].second, autoOffsets[4].second, autoOffsets[5].second)
                                                3 -> viewModel.updateSilentModeAutoTimes(autoOffsets[0].second, autoOffsets[1].second, autoOffsets[2].second, minutes, autoOffsets[4].second, autoOffsets[5].second)
                                                4 -> viewModel.updateSilentModeAutoTimes(autoOffsets[0].second, autoOffsets[1].second, autoOffsets[2].second, autoOffsets[3].second, minutes, autoOffsets[5].second)
                                                5 -> viewModel.updateSilentModeAutoTimes(autoOffsets[0].second, autoOffsets[1].second, autoOffsets[2].second, autoOffsets[3].second, autoOffsets[4].second, minutes)
                                            }
                                        },
                                        label = { Text("${pair.first} offset (min)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        enabled = salatEnabled[index]
                                    )
                                    OutlinedTextField(
                                        value = durationText,
                                        onValueChange = { newValue ->
                                            durationText = newValue
                                            val minutes = newValue.toIntOrNull() ?: 10
                                            when (index) {
                                                0 -> viewModel.updateSilentModeDurations(minutes, durations[1], durations[2], durations[3], durations[4], durations[5])
                                                1 -> viewModel.updateSilentModeDurations(durations[0], minutes, durations[2], durations[3], durations[4], durations[5])
                                                2 -> viewModel.updateSilentModeDurations(durations[0], durations[1], minutes, durations[3], durations[4], durations[5])
                                                3 -> viewModel.updateSilentModeDurations(durations[0], durations[1], durations[2], minutes, durations[4], durations[5])
                                                4 -> viewModel.updateSilentModeDurations(durations[0], durations[1], durations[2], durations[3], minutes, durations[5])
                                                5 -> viewModel.updateSilentModeDurations(durations[0], durations[1], durations[2], durations[3], durations[4], minutes)
                                            }
                                        },
                                        label = { Text("Duration (min)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        enabled = salatEnabled[index]
                                    )
                                    Switch(
                                        checked = salatEnabled[index],
                                        onCheckedChange = { checkAndToggleSilentMode(index, it) },
                                        modifier = Modifier.scale(0.8f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        SettingsCard(title = "Manual Timings") {
                            Text(
                                text = "Enter clock times (e.g., 05:30) for local mosque timings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            val durations = listOf(
                                userSettings.silentModeFajrDuration,
                                userSettings.silentModeDhuhrDuration,
                                userSettings.silentModeAsrDuration,
                                userSettings.silentModeMaghribDuration,
                                userSettings.silentModeIshaDuration,
                                userSettings.silentModeJumahDuration
                            )
                            
                            val salatEnabled = listOf(
                                userSettings.silentModeFajrEnabled,
                                userSettings.silentModeDhuhrEnabled,
                                userSettings.silentModeAsrEnabled,
                                userSettings.silentModeMaghribEnabled,
                                userSettings.silentModeIshaEnabled,
                                userSettings.silentModeJumahEnabled
                            )
                            
                            prayers.forEachIndexed { index, pair ->
                                var durationText by remember { mutableStateOf(durations[index].toString()) }
                                LaunchedEffect(durations[index]) {
                                    val currentInt = durationText.toIntOrNull() ?: 10
                                    val newInt = durations[index].toString().toIntOrNull() ?: 10
                                    if (currentInt != newInt) {
                                        durationText = durations[index].toString()
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable(
                                                enabled = salatEnabled[index],
                                                onClick = { showTimePickerFor = index }
                                            )
                                    ) {
                                        OutlinedTextField(
                                            value = pair.second,
                                            onValueChange = { },
                                            label = { Text("${pair.first} Time") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            readOnly = true,
                                            enabled = false,
                                            colors = if (salatEnabled[index]) {
                                                OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            } else {
                                                OutlinedTextFieldDefaults.colors()
                                            }
                                        )
                                    }
                                    OutlinedTextField(
                                        value = durationText,
                                        onValueChange = { newValue ->
                                            durationText = newValue
                                            val minutes = newValue.toIntOrNull() ?: 10
                                            when (index) {
                                                0 -> viewModel.updateSilentModeDurations(minutes, durations[1], durations[2], durations[3], durations[4], durations[5])
                                                1 -> viewModel.updateSilentModeDurations(durations[0], minutes, durations[2], durations[3], durations[4], durations[5])
                                                2 -> viewModel.updateSilentModeDurations(durations[0], durations[1], minutes, durations[3], durations[4], durations[5])
                                                3 -> viewModel.updateSilentModeDurations(durations[0], durations[1], durations[2], minutes, durations[4], durations[5])
                                                4 -> viewModel.updateSilentModeDurations(durations[0], durations[1], durations[2], durations[3], minutes, durations[5])
                                                5 -> viewModel.updateSilentModeDurations(durations[0], durations[1], durations[2], durations[3], durations[4], minutes)
                                            }
                                        },
                                        label = { Text("Duration (min)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        enabled = salatEnabled[index]
                                    )
                                    Switch(
                                        checked = salatEnabled[index],
                                        onCheckedChange = { checkAndToggleSilentMode(index, it) },
                                        modifier = Modifier.scale(0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }

    if (showTimePickerFor != null) {
        val index = showTimePickerFor!!
        val pair = prayers[index]
        val parts = pair.second.split(":")
        val calendar = Calendar.getInstance()
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: calendar.get(Calendar.MINUTE)
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = userSettings.is24HourFormat
        )

        ComposeTimePickerDialog(
            title = "Select ${pair.first} Time",
            onCancel = { showTimePickerFor = null },
            onConfirm = {
                val newValue = String.format(java.util.Locale.US, "%02d:%02d", timePickerState.hour, timePickerState.minute)
                when (index) {
                    0 -> viewModel.updateSilentModeManualTimes(newValue, prayers[1].second, prayers[2].second, prayers[3].second, prayers[4].second, prayers[5].second)
                    1 -> viewModel.updateSilentModeManualTimes(prayers[0].second, newValue, prayers[2].second, prayers[3].second, prayers[4].second, prayers[5].second)
                    2 -> viewModel.updateSilentModeManualTimes(prayers[0].second, prayers[1].second, newValue, prayers[3].second, prayers[4].second, prayers[5].second)
                    3 -> viewModel.updateSilentModeManualTimes(prayers[0].second, prayers[1].second, prayers[2].second, newValue, prayers[4].second, prayers[5].second)
                    4 -> viewModel.updateSilentModeManualTimes(prayers[0].second, prayers[1].second, prayers[2].second, prayers[3].second, newValue, prayers[5].second)
                    5 -> viewModel.updateSilentModeManualTimes(prayers[0].second, prayers[1].second, prayers[2].second, prayers[3].second, prayers[4].second, newValue)
                }
                showTimePickerFor = null
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}
