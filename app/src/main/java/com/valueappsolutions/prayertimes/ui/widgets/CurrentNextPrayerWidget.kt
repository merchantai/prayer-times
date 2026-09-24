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
import androidx.glance.text.TextStyle
import com.valueappsolutions.prayertimes.data.local.UserPreferencesRepository
import com.valueappsolutions.prayertimes.domain.PrayerData
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CurrentNextPrayerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val userPrefs = UserPreferencesRepository(context)
        
        val prayerData = userPrefs.cachedPrayerDataFlow.first()
        val is24HourFormat = userPrefs.userSettingsFlow.first().is24HourFormat

        provideContent {
            GlanceTheme(colors = WidgetThemeColors) {
                if (prayerData != null) {
                    CurrentNextPrayerContent(prayerData, is24HourFormat)
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
fun CurrentNextPrayerContent(prayerData: PrayerData, is24HourFormat: Boolean) {
    val allPrayers = WidgetUtils.getSortedPrayers(prayerData)
    val (currentPrayer, nextPrayer, nextPrayerTime) = WidgetUtils.getCurrentAndNextPrayer(allPrayers, LocalTime.now())
    
    val formattedNextTime = WidgetUtils.formatDisplayTime(nextPrayerTime, is24HourFormat)

    Column(
        modifier = GlanceModifier.fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Next: $nextPrayer",
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 16.sp
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = formattedNextTime,
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Current: $currentPrayer",
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 14.sp
            )
        )
    }
}
