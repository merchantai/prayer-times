package com.valueappsolutions.prayertimes.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import com.valueappsolutions.prayertimes.ui.components.ComposeTimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: PrayerViewModel,
    onNavigateBack: () -> Unit
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current
    var showTimePickerFor by remember { mutableStateOf<Int?>(null) }
    
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    var pendingNotificationIndex by remember { mutableStateOf<Int?>(null) }
    var pendingNotificationState by remember { mutableStateOf(false) }
    
    val fullScreenLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT >= 34 && notificationManager.canUseFullScreenIntent()) {
            pendingNotificationIndex?.let { index ->
                if (index == -1) viewModel.updateNotificationsEnabled(pendingNotificationState)
                else viewModel.updateNotificationEnabledForSalat(index, pendingNotificationState)
            }
        } else {
            Toast.makeText(context, "Full Screen Intent permission is required", Toast.LENGTH_SHORT).show()
        }
        pendingNotificationIndex = null
    }

    val exactAlarmLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            if (Build.VERSION.SDK_INT >= 34 && !notificationManager.canUseFullScreenIntent()) {
                Toast.makeText(context, "Please allow Full Screen Intents for alarms", Toast.LENGTH_LONG).show()
                val intent = Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT").apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                fullScreenLauncher.launch(intent)
            } else {
                pendingNotificationIndex?.let { index ->
                    if (index == -1) viewModel.updateNotificationsEnabled(pendingNotificationState)
                    else viewModel.updateNotificationEnabledForSalat(index, pendingNotificationState)
                }
                pendingNotificationIndex = null
            }
        } else {
            Toast.makeText(context, "Exact Alarm permission is required", Toast.LENGTH_SHORT).show()
            pendingNotificationIndex = null
        }
    }

    val postNotificationsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "Please allow Exact Alarms for prayer notifications", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                exactAlarmLauncher.launch(intent)
            } else if (Build.VERSION.SDK_INT >= 34 && !notificationManager.canUseFullScreenIntent()) {
                Toast.makeText(context, "Please allow Full Screen Intents for alarms", Toast.LENGTH_LONG).show()
                val intent = Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT").apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                fullScreenLauncher.launch(intent)
            } else {
                pendingNotificationIndex?.let { index ->
                    if (index == -1) viewModel.updateNotificationsEnabled(pendingNotificationState)
                    else viewModel.updateNotificationEnabledForSalat(index, pendingNotificationState)
                }
                pendingNotificationIndex = null
            }
        } else {
            Toast.makeText(context, "Notification permission is required", Toast.LENGTH_SHORT).show()
            pendingNotificationIndex = null
        }
    }

    val checkAndToggleNotification: (Int, Boolean) -> Unit = { index, enabled ->
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                pendingNotificationIndex = index
                pendingNotificationState = enabled
                postNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                pendingNotificationIndex = index
                pendingNotificationState = enabled
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                Toast.makeText(context, "Please allow Exact Alarms for prayer notifications", Toast.LENGTH_LONG).show()
                exactAlarmLauncher.launch(intent)
            } else if (Build.VERSION.SDK_INT >= 34 && !notificationManager.canUseFullScreenIntent()) {
                pendingNotificationIndex = index
                pendingNotificationState = enabled
                val intent = Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT").apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                Toast.makeText(context, "Please allow Full Screen Intents for alarms", Toast.LENGTH_LONG).show()
                fullScreenLauncher.launch(intent)
            } else {
                if (index == -1) viewModel.updateNotificationsEnabled(enabled)
                else viewModel.updateNotificationEnabledForSalat(index, enabled)
            }
        } else {
            if (index == -1) viewModel.updateNotificationsEnabled(enabled)
            else viewModel.updateNotificationEnabledForSalat(index, enabled)
        }
    }
    
    val prayers = listOf(
        "Fajr" to userSettings.manualFajrTime,
        "Dhuhr" to userSettings.manualDhuhrTime,
        "Asr" to userSettings.manualAsrTime,
        "Maghrib" to userSettings.manualMaghribTime,
        "Isha" to userSettings.manualIshaTime,
        "Jumah" to userSettings.manualJumahTime
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings", style = MaterialTheme.typography.titleLarge) },
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
                            text = "Enable Notifications",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = userSettings.notificationsEnabled,
                            onCheckedChange = { checkAndToggleNotification(-1, it) }
                        )
                    }
                }
            }

            if (userSettings.notificationsEnabled) {
                item {
                    SettingsCard(title = "Notification Mode") {
                        val modes = listOf("Automatic", "Manual")
                        modes.forEachIndexed { index, mode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateNotificationMode(index) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = userSettings.notificationMode == index,
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

                if (userSettings.notificationMode == 0) {
                    item {
                        SettingsCard(title = "Alarm Offsets") {
                            Text(
                                text = "Enter minutes after the Adhan to ring the alarm. (e.g. 0 for Maghrib to ring at sunset)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            val autoOffsets = listOf(
                                "Fajr" to userSettings.notificationAutoFajrMinutes.toString(),
                                "Dhuhr" to userSettings.notificationAutoDhuhrMinutes.toString(),
                                "Asr" to userSettings.notificationAutoAsrMinutes.toString(),
                                "Maghrib" to userSettings.notificationAutoMaghribMinutes.toString(),
                                "Isha" to userSettings.notificationAutoIshaMinutes.toString(),
                                "Jumah" to userSettings.notificationAutoJumahMinutes.toString()
                            )

                            val salatEnabled = listOf(
                                userSettings.notificationFajrEnabled,
                                userSettings.notificationDhuhrEnabled,
                                userSettings.notificationAsrEnabled,
                                userSettings.notificationMaghribEnabled,
                                userSettings.notificationIshaEnabled,
                                userSettings.notificationJumahEnabled
                            )

                            autoOffsets.forEachIndexed { index, pair ->
                                var textValue by remember { mutableStateOf(pair.second) }
                                LaunchedEffect(pair.second) {
                                    val currentInt = textValue.toIntOrNull() ?: 0
                                    val newInt = pair.second.toIntOrNull() ?: 0
                                    if (currentInt != newInt) {
                                        textValue = pair.second
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
                                        value = textValue,
                                        onValueChange = { newValue ->
                                            textValue = newValue
                                            val minutes = newValue.toIntOrNull() ?: 0
                                            when (index) {
                                                0 -> viewModel.updateNotificationAutoTimes(minutes, autoOffsets[1].second.toIntOrNull() ?: 0, autoOffsets[2].second.toIntOrNull() ?: 0, autoOffsets[3].second.toIntOrNull() ?: 0, autoOffsets[4].second.toIntOrNull() ?: 0, autoOffsets[5].second.toIntOrNull() ?: 0)
                                                1 -> viewModel.updateNotificationAutoTimes(autoOffsets[0].second.toIntOrNull() ?: 0, minutes, autoOffsets[2].second.toIntOrNull() ?: 0, autoOffsets[3].second.toIntOrNull() ?: 0, autoOffsets[4].second.toIntOrNull() ?: 0, autoOffsets[5].second.toIntOrNull() ?: 0)
                                                2 -> viewModel.updateNotificationAutoTimes(autoOffsets[0].second.toIntOrNull() ?: 0, autoOffsets[1].second.toIntOrNull() ?: 0, minutes, autoOffsets[3].second.toIntOrNull() ?: 0, autoOffsets[4].second.toIntOrNull() ?: 0, autoOffsets[5].second.toIntOrNull() ?: 0)
                                                3 -> viewModel.updateNotificationAutoTimes(autoOffsets[0].second.toIntOrNull() ?: 0, autoOffsets[1].second.toIntOrNull() ?: 0, autoOffsets[2].second.toIntOrNull() ?: 0, minutes, autoOffsets[4].second.toIntOrNull() ?: 0, autoOffsets[5].second.toIntOrNull() ?: 0)
                                                4 -> viewModel.updateNotificationAutoTimes(autoOffsets[0].second.toIntOrNull() ?: 0, autoOffsets[1].second.toIntOrNull() ?: 0, autoOffsets[2].second.toIntOrNull() ?: 0, autoOffsets[3].second.toIntOrNull() ?: 0, minutes, autoOffsets[5].second.toIntOrNull() ?: 0)
                                                5 -> viewModel.updateNotificationAutoTimes(autoOffsets[0].second.toIntOrNull() ?: 0, autoOffsets[1].second.toIntOrNull() ?: 0, autoOffsets[2].second.toIntOrNull() ?: 0, autoOffsets[3].second.toIntOrNull() ?: 0, autoOffsets[4].second.toIntOrNull() ?: 0, minutes)
                                            }
                                        },
                                        label = { Text("${pair.first} Offset (mins)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        enabled = salatEnabled[index]
                                    )
                                    IconButton(
                                        onClick = { checkAndToggleNotification(index, !salatEnabled[index]) }
                                    ) {
                                        Icon(
                                            imageVector = if (salatEnabled[index]) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                                            contentDescription = if (salatEnabled[index]) "Mute" else "Unmute",
                                            tint = if (salatEnabled[index]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
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
                            
                            val salatEnabled = listOf(
                                userSettings.notificationFajrEnabled,
                                userSettings.notificationDhuhrEnabled,
                                userSettings.notificationAsrEnabled,
                                userSettings.notificationMaghribEnabled,
                                userSettings.notificationIshaEnabled,
                                userSettings.notificationJumahEnabled
                            )
                            
                            prayers.forEachIndexed { index, pair ->
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
                                    IconButton(
                                        onClick = { checkAndToggleNotification(index, !salatEnabled[index]) }
                                    ) {
                                        Icon(
                                            imageVector = if (salatEnabled[index]) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                                            contentDescription = if (salatEnabled[index]) "Mute" else "Unmute",
                                            tint = if (salatEnabled[index]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    SettingsCard(title = "Notification Type") {
                        val types = listOf("Ring (Azaan)", "Vibrate", "Both")
                        types.forEachIndexed { index, type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateNotificationType(index) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = userSettings.notificationType == index,
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
                    0 -> viewModel.updateManualPrayerTimes(newValue, prayers[1].second, prayers[2].second, prayers[3].second, prayers[4].second, prayers[5].second)
                    1 -> viewModel.updateManualPrayerTimes(prayers[0].second, newValue, prayers[2].second, prayers[3].second, prayers[4].second, prayers[5].second)
                    2 -> viewModel.updateManualPrayerTimes(prayers[0].second, prayers[1].second, newValue, prayers[3].second, prayers[4].second, prayers[5].second)
                    3 -> viewModel.updateManualPrayerTimes(prayers[0].second, prayers[1].second, prayers[2].second, newValue, prayers[4].second, prayers[5].second)
                    4 -> viewModel.updateManualPrayerTimes(prayers[0].second, prayers[1].second, prayers[2].second, prayers[3].second, newValue, prayers[5].second)
                    5 -> viewModel.updateManualPrayerTimes(prayers[0].second, prayers[1].second, prayers[2].second, prayers[3].second, prayers[4].second, newValue)
                }
                showTimePickerFor = null
            }
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectorColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface,
                    periodSelectorBorderColor = MaterialTheme.colorScheme.primary,
                    periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                    periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
