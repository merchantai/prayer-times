package com.valueappsolutions.prayertimes.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Qibla
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaCompassScreen(
    viewModel: PrayerViewModel
) {
    val location by viewModel.currentLocation.collectAsState()
    
    val qiblaDirection = remember(location) {
        if (location != null) {
            val coords = Coordinates(location!!.first, location!!.second)
            Qibla(coords).direction.toFloat()
        } else {
            null
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        if (qiblaDirection == null) {
            Text(
                text = "Location not available. Please allow location permissions in Settings.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            QiblaCompassContent(qiblaDirection)
        }
    }
    }
}

@Composable
fun QiblaCompassContent(qiblaDirection: Float) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val rotationSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }

    var azimuth by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var sensorAccuracy by remember { androidx.compose.runtime.mutableIntStateOf(SensorManager.SENSOR_STATUS_UNRELIABLE) }
    val hasSensor = rotationSensor != null

    DisposableEffect(sensorManager, rotationSensor) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientationAngles = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    val azimuthInRadians = orientationAngles[0]
                    var azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
                    if (azimuthInDegrees < 0) azimuthInDegrees += 360f
                    azimuth = azimuthInDegrees
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                sensorAccuracy = accuracy
            }
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    if (!hasSensor) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Device compass sensor not available.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
        Text(
            text = "Qibla Bearing: ${String.format(java.util.Locale.US, "%.1f", qiblaDirection)}°",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    } else {
        if (sensorAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || sensorAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Please calibrate the compass by moving your device in a figure 8 motion.",
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Text(
            text = "Qibla is ${String.format(java.util.Locale.US, "%.1f", qiblaDirection)}° from North",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Compass Box
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // North/East/South/West Indicators
            val primaryColor = MaterialTheme.colorScheme.primary
            val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(360f - azimuth),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2 - 32.dp.toPx() // Make room for text
                    
                    // Draw outer ring
                    drawCircle(
                        color = onSurfaceColor.copy(alpha = 0.2f),
                        radius = radius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                    )
                }
                
                Text("N", modifier = Modifier.align(Alignment.TopCenter).padding(12.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("S", modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("E", modifier = Modifier.align(Alignment.CenterEnd).padding(12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("W", modifier = Modifier.align(Alignment.CenterStart).padding(12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            // Kaaba Pointer
            val kaabaRotation = (360 - azimuth) + qiblaDirection
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(kaabaRotation),
                contentAlignment = Alignment.TopCenter
            ) {
                Icon(
                    imageVector = Icons.Default.Warning, // Temporary placeholder for kaaba or arrow
                    contentDescription = "Kaaba Direction",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Center dot
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
