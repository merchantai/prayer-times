package com.valueappsolutions.prayertimes.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
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
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val userPrefs = UserPreferencesRepository(context)
        
        val prayerData = userPrefs.cachedPrayerDataFlow.first()
        val is24HourFormat = userPrefs.userSettingsFlow.first().is24HourFormat

        provideContent {
            GlanceTheme(colors = WidgetThemeColors) {
                if (prayerData != null) {
                    HorizontalFardPrayersContent(prayerData, is24HourFormat)
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
    val fardPrayers = listOf(
        "Fajr" to prayerData.fajr,
        "Dhuhr" to prayerData.dhuhr,
        "Asr" to prayerData.asr,
        "Maghrib" to prayerData.maghrib,
        "Isha" to prayerData.isha
    ).filter { it.second.isNotBlank() }

    val allPrayers = WidgetUtils.getSortedPrayers(prayerData)
    val (currentPrayer, _, _) = WidgetUtils.getCurrentAndNextPrayer(allPrayers, LocalTime.now())

    Row(
        modifier = GlanceModifier.fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        fardPrayers.forEachIndexed { index, (name, timeStr) ->
            val isCurrent = name == currentPrayer
            val formattedTime = WidgetUtils.formatDisplayTimeStr(timeStr, is24HourFormat)
            
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = name,
                    style = TextStyle(
                        color = if (isCurrent) GlanceTheme.colors.primary else GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = formattedTime,
                    style = TextStyle(
                        color = if (isCurrent) GlanceTheme.colors.primary else GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
