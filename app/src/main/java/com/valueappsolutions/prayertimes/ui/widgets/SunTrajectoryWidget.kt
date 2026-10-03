package com.valueappsolutions.prayertimes.ui.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.valueappsolutions.prayertimes.data.local.UserPreferencesRepository
import com.valueappsolutions.prayertimes.domain.PrayerData
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin
import com.valueappsolutions.prayertimes.MainActivity

class SunTrajectoryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SunTrajectoryWidget()
}

class SunTrajectoryWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val userPrefs = UserPreferencesRepository(context)

        provideContent {
            val prayerData by userPrefs.cachedPrayerDataFlow.collectAsState(initial = null)
            val userSettings by userPrefs.userSettingsFlow.collectAsState(initial = null)
            val is24HourFormat = userSettings?.is24HourFormat ?: false

            GlanceTheme(colors = WidgetThemeColors) {
                if (prayerData != null) {
                    SunTrajectoryContent(prayerData!!, is24HourFormat)
                } else {
                    Column(
                        modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.surface).padding(16.dp).clickable(actionStartActivity<MainActivity>()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Waiting for data...", style = TextStyle(color = GlanceTheme.colors.onSurface))
                    }
                }
            }
        }
    }
}

@Composable
fun SunTrajectoryContent(data: PrayerData, is24HourFormat: Boolean) {
    val size = LocalSize.current
    val scale = (size.width.value / 320f).coerceIn(0.7f, 1.5f)

    val titleSize = (14f * scale).sp
    val subtitleSize = (11f * scale).sp
    val boldTextStyle = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = subtitleSize, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

    val sunriseStr = data.sunrise
    val dhuhrStr = data.dhuhr
    val maghribStr = data.maghrib
    val moonFraction = data.moonFraction

    val parse = { t: String -> WidgetUtils.parseTime(t) }
    val formatDisplay = { t: LocalTime? -> WidgetUtils.formatDisplayTime(t, is24HourFormat) }

    val sunrise = parse(sunriseStr) ?: LocalTime.of(6, 0)
    val dhuhr = parse(dhuhrStr)
    val maghrib = parse(maghribStr) ?: LocalTime.of(18, 0)

    val currentTime = LocalTime.now()
    val totalMins = Duration.between(sunrise, maghrib).toMinutes().toFloat()
    val elapsedMins = Duration.between(sunrise, currentTime).toMinutes().toFloat()
    val progress = (elapsedMins / totalMins).coerceIn(0f, 1f)
    val phasePercent = (moonFraction * 100).toInt()

    Column(
        modifier = GlanceModifier.fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .clickable(actionStartActivity<MainActivity>())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val drawingHeight = (size.height.value - 40f).coerceAtLeast(50f)
        val bitmapWidth = (size.width.value * 2).toInt().coerceAtLeast(400)
        val bitmapHeight = (drawingHeight * 2).toInt().coerceAtLeast(100)
        val bitmap = createSunTrajectoryBitmap(bitmapWidth, bitmapHeight, progress, moonFraction.toFloat())

        Box(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = "Sun Trajectory",
                modifier = GlanceModifier.fillMaxSize()
            )
            
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(top = 2.dp),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Noon ${formatDisplay(dhuhr)}",
                    style = boldTextStyle
                )
            }
        }
        
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Sunrise", style = boldTextStyle)
                Text(formatDisplay(sunrise), style = boldTextStyle)
            }
            
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "$phasePercent%",
                    style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = subtitleSize, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                )
                Text(
                    "Moon Visibility",
                    style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = subtitleSize, textAlign = TextAlign.Center)
                )
            }

            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Sunset", style = boldTextStyle)
                Text(formatDisplay(maghrib), style = boldTextStyle)
            }
        }
    }
}

private fun createSunTrajectoryBitmap(width: Int, height: Int, progress: Float, moonFraction: Float): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // We use a gold/primary color that looks good on both light/dark themes
    val color = android.graphics.Color.parseColor("#D4AF37") 
    
    val centerX = width / 2f
    val centerY = height - 30f
    
    val radiusX = (width / 2f) - 64f
    // Keep the peak of the arc at a fixed distance from the top to perfectly align beneath the Noon text
    val topPadding = 45f
    val radiusY = (centerY - topPadding).coerceAtLeast(30f)
    
    val paintArc = Paint().apply {
        isAntiAlias = true
        this.color = color
        alpha = (255 * 0.2f).toInt()
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0f)
    }
    
    val rectF = RectF(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY)
    canvas.drawArc(rectF, 180f, 180f, false, paintArc)
    
    val paintNoon = Paint().apply {
        isAntiAlias = true
        this.color = color
        alpha = (255 * 0.5f).toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY - radiusY, 8f, paintNoon)
    
    if (progress in 0.001f..0.999f) {
        val angle = 180f + (progress * 180f)
        val angleRad = angle * (Math.PI / 180.0)
        val sunX = centerX + radiusX * cos(angleRad).toFloat()
        val sunY = centerY + radiusY * sin(angleRad).toFloat()
        
        val paintSun = Paint().apply {
            isAntiAlias = true
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawCircle(sunX, sunY, 12f, paintSun)
        
        val paintSunGlow = Paint().apply {
            isAntiAlias = true
            this.color = color
            alpha = (255 * 0.3f).toInt()
            style = Paint.Style.FILL
        }
        canvas.drawCircle(sunX, sunY, 24f, paintSunGlow)
    }
    
    val moonRadius = (radiusY * 0.45f).coerceIn(16f, 60f)
    val moonCenterY = centerY - (moonRadius * 0.5f)
    
    val paintMoonGlow = Paint().apply {
        isAntiAlias = true
        this.color = color
        alpha = (255 * 0.05f).toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, moonCenterY, moonRadius * 1.5f, paintMoonGlow)
    
    val paintMoonBase = Paint().apply {
        isAntiAlias = true
        this.color = color
        alpha = (255 * 0.1f).toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, moonCenterY, moonRadius, paintMoonBase)
    
    val wipeWidth = moonRadius * 2 * moonFraction
    canvas.save()
    canvas.clipRect(centerX - moonRadius, moonCenterY - moonRadius, centerX - moonRadius + wipeWidth, moonCenterY + moonRadius)
    
    val paintMoonSolid = Paint().apply {
        isAntiAlias = true
        this.color = color
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, moonCenterY, moonRadius, paintMoonSolid)
    canvas.restore()

    return bitmap
}
