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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.valueappsolutions.prayertimes.ui.components.ComposeTimePickerDialog
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerUiState
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijriSettingsScreen(
    viewModel: PrayerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current

    var currentHijriOffset by remember { mutableIntStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is PrayerUiState.Success) {
            val state = uiState as PrayerUiState.Success
            currentHijriOffset = state.hijriOffset
        }
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    var pendingNotificationState by remember { mutableStateOf(false) }

    val fullScreenLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT >= 34 && notificationManager.canUseFullScreenIntent()) {
            viewModel.updateAyyamEBeedReminderEnabled(pendingNotificationState)
        } else {
            Toast.makeText(context, "Full Screen Intent permission is required", Toast.LENGTH_SHORT).show()
        }
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
                viewModel.updateAyyamEBeedReminderEnabled(pendingNotificationState)
            }
        } else {
            Toast.makeText(context, "Exact Alarm permission is required", Toast.LENGTH_SHORT).show()
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
                viewModel.updateAyyamEBeedReminderEnabled(pendingNotificationState)
            }
        } else {
            Toast.makeText(context, "Notification permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    val checkAndToggleReminder: (Boolean) -> Unit = { enabled ->
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                pendingNotificationState = enabled
                postNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                pendingNotificationState = enabled
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                Toast.makeText(context, "Please allow Exact Alarms for reminders", Toast.LENGTH_LONG).show()
                exactAlarmLauncher.launch(intent)
            } else if (Build.VERSION.SDK_INT >= 34 && !notificationManager.canUseFullScreenIntent()) {
                pendingNotificationState = enabled
                val intent = Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT").apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                Toast.makeText(context, "Please allow Full Screen Intents for alarms", Toast.LENGTH_LONG).show()
                fullScreenLauncher.launch(intent)
            } else {
                viewModel.updateAyyamEBeedReminderEnabled(enabled)
            }
        } else {
            viewModel.updateAyyamEBeedReminderEnabled(enabled)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hijri Settings", style = MaterialTheme.typography.titleLarge) },
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
                    SettingsCard(title = "Hijri Date Offset (Days)") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "$currentHijriOffset", color = MaterialTheme.colorScheme.onBackground)
                            Slider(
                                value = currentHijriOffset.toFloat(),
                                onValueChange = { 
                                    currentHijriOffset = it.toInt()
                                },
                                onValueChangeFinished = {
                                    viewModel.updateHijriOffset(currentHijriOffset)
                                },
                                valueRange = -2f..2f,
                                steps = 3,
                                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                                )
                            )
                        }
                    }
                }
                item {
                    SettingsCard(title = "Ayyam-e-Beed Reminder") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Remind me a day before Ayyam-e-Beed",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f).padding(end = 16.dp)
                                )
                                Switch(
                                    checked = userSettings.ayyamEBeedReminderEnabled,
                                    onCheckedChange = { checkAndToggleReminder(it) }
                                )
                            }
                            
                            if (userSettings.ayyamEBeedReminderEnabled) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showTimePicker = true }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Reminder Time",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = userSettings.ayyamEBeedReminderTime,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val parts = userSettings.ayyamEBeedReminderTime.split(":")
        val calendar = Calendar.getInstance()
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: calendar.get(Calendar.MINUTE)
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = userSettings.is24HourFormat
        )

        ComposeTimePickerDialog(
            title = "Select Reminder Time",
            onCancel = { showTimePicker = false },
            onConfirm = {
                val newValue = String.format(java.util.Locale.US, "%02d:%02d", timePickerState.hour, timePickerState.minute)
                viewModel.updateAyyamEBeedReminderTime(newValue)
                showTimePicker = false
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
