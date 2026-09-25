package com.valueappsolutions.prayertimes.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.PathEffect
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import com.valueappsolutions.prayertimes.domain.PrayerData
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: PrayerUiState,
    isDarkMode: Boolean,
    is24HourFormat: Boolean,
    isExpanded: Boolean = false,
    onToggleTheme: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onRequestLocation: () -> Unit,
    onRefresh: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prayer Times", style = MaterialTheme.typography.titleLarge) },

                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                    IconButton(onClick = onRequestLocation) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Update Location")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when (uiState) {
                is PrayerUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is PrayerUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Location required or Error occurred.", color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRequestLocation,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Retry / Request Permission", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
                is PrayerUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        DashboardContent(uiState.prayerData, uiState.isRefreshing, is24HourFormat, isExpanded)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardContent(data: PrayerData, isRefreshing: Boolean, is24HourFormat: Boolean, isExpanded: Boolean = false, showRealtimeInfo: Boolean = true) {
    fun formatDisplayTime(timeStr: String): String {
        return com.valueappsolutions.prayertimes.ui.widgets.WidgetUtils.formatDisplayTimeStr(timeStr, is24HourFormat)
    }

    val fardPrayers = listOf(
        "Fajr" to Pair(data.fajr, data.fajrLocal),
        "Dhuhr" to Pair(data.dhuhr, data.dhuhrLocal),
        "Asr" to Pair(data.asr, data.asrLocal),
        "Maghrib" to Pair(data.maghrib, data.maghribLocal),
        "Isha" to Pair(data.isha, data.ishaLocal)
    ).filter { it.second.first.isNotBlank() }

    val naflPrayers = listOf(
        "Ishraq" to Pair(data.ishraq, data.ishraqLocal),
        "Chasht" to Pair(data.chasht, data.chashtLocal),
        "Tahajjud" to Pair(data.tahajjud, data.tahajjudLocal)
    ).filter { it.second.first.isNotBlank() }

    // Combine for next prayer logic including Sunrise
    val allPrayers = remember(data) {
        com.valueappsolutions.prayertimes.ui.widgets.WidgetUtils.getSortedPrayers(data)
    }

    // Determine current prayer initially just for highlighting the cards (doesn't need per-second updates)
    val initialTime = LocalTime.now()
    val (currentPrayerStatic, _, _) = com.valueappsolutions.prayertimes.ui.widgets.WidgetUtils.getCurrentAndNextPrayer(allPrayers, initialTime)

    val locationText = if (data.cityName.isNotBlank() && data.areaName.isNotBlank()) {
        "${data.areaName}, ${data.cityName}"
    } else if (data.cityName.isNotBlank()) {
        data.cityName
    } else {
        "Unknown Location"
    }

    if (isExpanded) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left column (Hero + Sun)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (showRealtimeInfo) {
                    RealtimeHeroSection(data, isRefreshing, locationText, allPrayers, showRealtimeInfo)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                RealtimeSunTrajectorySection(data, is24HourFormat)
            }
            
            // Right column (Prayers List)
            LazyColumn(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (fardPrayers.isNotEmpty()) {
                    item {
                        SectionHeader("Obligatory Prayers (Fard)")
                    }
                    items(fardPrayers.size) { index ->
                        val (name, times) = fardPrayers[index]
                        val (time, local) = times
                        val formattedLocal = if (local.isNotBlank()) formatDisplayTime(local) else ""
                        PrayerCard(name = name, time = formatDisplayTime(time), localTime = formattedLocal, isCurrent = if (showRealtimeInfo) name == currentPrayerStatic else false)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
        
                if (naflPrayers.isNotEmpty()) {
                    item {
                        SectionHeader("Voluntary Prayers (Nafl)")
                    }
                    items(naflPrayers.size) { index ->
                        val (name, times) = naflPrayers[index]
                        val (time, local) = times
                        val formattedLocal = if (local.isNotBlank()) formatDisplayTime(local) else ""
                        PrayerCard(name = name, time = formatDisplayTime(time), localTime = formattedLocal, isCurrent = if (showRealtimeInfo) name == currentPrayerStatic else false)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Section
            if (showRealtimeInfo) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    RealtimeHeroSection(data, isRefreshing, locationText, allPrayers, showRealtimeInfo)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
    
            item {
                RealtimeSunTrajectorySection(data, is24HourFormat)
                Spacer(modifier = Modifier.height(16.dp))
            }
    
            // Fard Prayers Section
            if (fardPrayers.isNotEmpty()) {
                item {
                    SectionHeader("Obligatory Prayers (Fard)")
                }
                items(fardPrayers.size) { index ->
                    val (name, times) = fardPrayers[index]
                    val (time, local) = times
                    val formattedLocal = if (local.isNotBlank()) formatDisplayTime(local) else ""
                    PrayerCard(name = name, time = formatDisplayTime(time), localTime = formattedLocal, isCurrent = if (showRealtimeInfo) name == currentPrayerStatic else false)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
    
            // Nafl Prayers Section
            if (naflPrayers.isNotEmpty()) {
                item {
                    SectionHeader("Voluntary Prayers (Nafl)")
                }
                items(naflPrayers.size) { index ->
                    val (name, times) = naflPrayers[index]
                    val (time, local) = times
                    val formattedLocal = if (local.isNotBlank()) formatDisplayTime(local) else ""
                    PrayerCard(name = name, time = formatDisplayTime(time), localTime = formattedLocal, isCurrent = if (showRealtimeInfo) name == currentPrayerStatic else false)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun RealtimeHeroSection(data: PrayerData, isRefreshing: Boolean, locationText: String, allPrayers: List<Pair<String, LocalTime>>, showRealtimeInfo: Boolean = true) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while(true) {
            delay(1000)
            currentTime = LocalTime.now()
        }
    }

    val (currentPrayer, nextPrayer, nextPrayerTime) = remember(allPrayers, currentTime) {
        com.valueappsolutions.prayertimes.ui.widgets.WidgetUtils.getCurrentAndNextPrayer(allPrayers, currentTime)
    }

    var timeRemainingStr = ""
    if (nextPrayerTime != null) {
        var duration = Duration.between(currentTime, nextPrayerTime)
        if (duration.isNegative) {
            duration = duration.plusHours(24)
        }
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        timeRemainingStr = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    HeroSection(
        currentDate = data.currentDate,
        hijriDate = data.hijriDate,
        location = locationText,
        currentPrayer = currentPrayer,
        nextPrayer = nextPrayer,
        timeRemaining = timeRemainingStr,
        isOffline = data.isOfflineFallback,
        isRefreshing = isRefreshing,
        lastUpdatedTime = data.lastUpdatedTime,
        showRealtimeInfo = showRealtimeInfo
    )
}

@Composable
fun RealtimeSunTrajectorySection(data: PrayerData, is24HourFormat: Boolean) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while(true) {
            delay(60000) // Update every minute instead of every second to reduce canvas redraws
            currentTime = LocalTime.now()
        }
    }

    SunTrajectorySection(
        sunriseStr = data.sunrise,
        dhuhrStr = data.dhuhr,
        maghribStr = data.maghrib,
        currentTime = currentTime,
        moonFraction = data.moonFraction,
        is24HourFormat = is24HourFormat
    )
}

@Composable
fun HeroSection(
    currentDate: String,
    hijriDate: String,
    location: String,
    currentPrayer: String,
    nextPrayer: String,
    timeRemaining: String,
    isOffline: Boolean,
    isRefreshing: Boolean,
    lastUpdatedTime: Long,
    showRealtimeInfo: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = hijriDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = location,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                if (showRealtimeInfo) {
                    Text(
                        text = "Next: $nextPrayer",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Text(
                        text = timeRemaining,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Current: $currentPrayer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }



                if (lastUpdatedTime > 0) {
                    val formatter = java.text.SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                    val formattedTime = formatter.format(java.util.Date(lastUpdatedTime))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Last updated: $formattedTime",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        textAlign = TextAlign.Start
    )
}

@Composable
fun PrayerCard(name: String, time: String, localTime: String = "", isCurrent: Boolean) {
    val containerColor = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onSurface
    val borderColor = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (localTime.isNotEmpty()) 88.dp else 72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    color = contentColor
                )
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else contentColor
                    )
                )
                if (localTime.isNotEmpty()) {
                    Text(
                        text = "Local: $localTime",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SunTrajectorySection(sunriseStr: String, dhuhrStr: String, maghribStr: String, currentTime: LocalTime, moonFraction: Double, is24HourFormat: Boolean) {
    val parse = { t: String -> com.valueappsolutions.prayertimes.ui.widgets.WidgetUtils.parseTime(t) }
    fun formatDisplay(t: LocalTime?): String {
        return com.valueappsolutions.prayertimes.ui.widgets.WidgetUtils.formatDisplayTime(t, is24HourFormat)
    }
    
    val sunrise = parse(sunriseStr) ?: LocalTime.of(6, 0)
    val dhuhr = parse(dhuhrStr)
    val maghrib = parse(maghribStr) ?: LocalTime.of(18, 0)

    val totalMins = Duration.between(sunrise, maghrib).toMinutes().toFloat()
    val elapsedMins = Duration.between(sunrise, currentTime).toMinutes().toFloat()
    val progress = (elapsedMins / totalMins).coerceIn(0f, 1f)
    
    // Moon phase calculation using fraction from repository
    val phasePercent = (moonFraction * 100).toInt()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        val canvasWidth = maxWidth
        val radiusX = (canvasWidth / 2) - 32.dp
        val radiusY = 60.dp // Flatter curve
        val canvasHeight = radiusY + 96.dp
        
        Box(modifier = Modifier.size(canvasWidth, canvasHeight)) {
            val primaryColor = MaterialTheme.colorScheme.primary
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height - 60.dp.toPx())
                val pxRadiusX = radiusX.toPx()
                val pxRadiusY = radiusY.toPx()
                
                // Draw arc
                drawArc(
                    color = primaryColor.copy(alpha = 0.2f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(center.x - pxRadiusX, center.y - pxRadiusY),
                    size = Size(pxRadiusX * 2, pxRadiusY * 2),
                    style = Stroke(
                        width = 4.dp.toPx(), 
                        cap = StrokeCap.Round, 
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
                    )
                )
                
                // Draw moon
                val moonRadius = 24.dp.toPx()
                val moonCenter = Offset(center.x, center.y - 4.dp.toPx())
                
                // Moon Glow
                drawCircle(
                    color = primaryColor.copy(alpha = 0.05f),
                    radius = moonRadius * 1.5f,
                    center = moonCenter
                )
                
                // Moon Base (Dark part)
                drawCircle(
                    color = primaryColor.copy(alpha = 0.1f),
                    radius = moonRadius,
                    center = moonCenter
                )
                
                // Calculate clip bounds to create crescent shape
                val wipeWidth = moonRadius * 2 * moonFraction.toFloat()
                clipRect(
                    left = center.x - moonRadius,
                    top = moonCenter.y - moonRadius,
                    right = center.x - moonRadius + wipeWidth,
                    bottom = moonCenter.y + moonRadius
                ) {
                    drawCircle(
                        color = primaryColor,
                        radius = moonRadius,
                        center = moonCenter
                    )
                }
                
                // Draw sun only if between sunrise and sunset
                if (progress in 0.001f..0.999f && currentTime.isBefore(maghrib)) {
                    val angle = 180f + (progress * 180f)
                    val angleRad = angle * (PI / 180.0)
                    val sunX = center.x + pxRadiusX * cos(angleRad).toFloat()
                    val sunY = center.y + pxRadiusY * sin(angleRad).toFloat() // sin is negative here since angle is 180..360
                    
                    drawCircle(
                        color = primaryColor,
                        radius = 8.dp.toPx(),
                        center = Offset(sunX, sunY)
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.3f),
                        radius = 16.dp.toPx(),
                        center = Offset(sunX, sunY)
                    )
                }
                
                // Draw noon marker
                val noonX = center.x
                val noonY = center.y - pxRadiusY
                drawCircle(
                    color = primaryColor.copy(alpha = 0.5f),
                    radius = 4.dp.toPx(),
                    center = Offset(noonX, noonY)
                )
            }
            
            // Text for moon percentage
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "$phasePercent%",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    "Moon Visibility",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Text(
                "Sunrise\n${formatDisplay(sunrise)}", 
                modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 0.dp), 
                textAlign = TextAlign.Center, 
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), 
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Noon ${formatDisplay(dhuhr)}", 
                modifier = Modifier.align(Alignment.TopCenter), 
                textAlign = TextAlign.Center, 
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), 
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Sunset\n${formatDisplay(maghrib)}", 
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 0.dp), 
                textAlign = TextAlign.Center, 
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), 
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
