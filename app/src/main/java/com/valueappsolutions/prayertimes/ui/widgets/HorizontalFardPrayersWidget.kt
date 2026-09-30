package com.valueappsolutions.prayertimes.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.SizeMode
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
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
import java.time.LocalTime

class HorizontalFardPrayersWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val userPrefs = UserPreferencesRepository(context)

        provideContent {
            val prayerData by userPrefs.cachedPrayerDataFlow.collectAsState(initial = null)
            val userSettings by userPrefs.userSettingsFlow.collectAsState(initial = null)
            val is24HourFormat = userSettings?.is24HourFormat ?: false

            GlanceTheme(colors = WidgetThemeColors) {
                if (prayerData != null) {
                    HorizontalFardPrayersContent(prayerData!!, is24HourFormat)
                } else {
                    Column(
                        modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.surface).padding(16.dp),
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
fun HorizontalFardPrayersContent(prayerData: PrayerData, is24HourFormat: Boolean) {
    val size = LocalSize.current
    val scale = (size.width.value / 320f).coerceIn(0.7f, 1.5f)
    
    val titleSize = (10f * scale).sp
    val nameSize = (9f * scale).sp
    val timeSize = (11f * scale).sp
    val spacerHeight = (0.5f * scale).dp

    val fardPrayers = listOf(
        "Fajr" to prayerData.fajr,
        "Sunrise" to prayerData.sunrise,
        "Dhuhr" to prayerData.dhuhr,
        "Asr" to prayerData.asr,
        "Maghrib" to prayerData.maghrib,
        "Isha" to prayerData.isha
    ).filter { it.second.isNotBlank() }

    val allPrayers = WidgetUtils.getSortedPrayers(prayerData)
    val (currentPrayer, _, _) = WidgetUtils.getCurrentAndNextPrayer(allPrayers, LocalTime.now())

    val locationName = if (prayerData.cityName.isNotBlank()) {
        if (prayerData.areaName.isNotBlank()) "${prayerData.areaName}, ${prayerData.cityName}" else prayerData.cityName
    } else {
        "Unknown Location"
    }

    val hijriDateStr = prayerData.hijriDate

    Column(
        modifier = GlanceModifier.fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Top Row: Location and Hijri Date
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = locationName,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = nameSize,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = hijriDateStr,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = nameSize,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
            )
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp * scale))
        
        // Prayers Row
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            fardPrayers.forEachIndexed { index, (name, timeStr) ->
                val isCurrent = name == currentPrayer
                val formattedTime = WidgetUtils.formatDisplayTimeStr(timeStr, is24HourFormat)
                
                Column(
                    modifier = GlanceModifier.defaultWeight().padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = name,
                        style = TextStyle(
                            color = if (isCurrent) GlanceTheme.colors.primary else GlanceTheme.colors.onSurface,
                            fontSize = nameSize,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(spacerHeight))
                    Text(
                        text = formattedTime,
                        style = TextStyle(
                            color = if (isCurrent) GlanceTheme.colors.primary else GlanceTheme.colors.onSurface,
                            fontSize = timeSize,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}
