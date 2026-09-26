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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.valueappsolutions.prayertimes.domain.PrayerData
import com.valueappsolutions.prayertimes.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.first

class HijriDateWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val userPrefs = UserPreferencesRepository(context)
        val prayerData = userPrefs.cachedPrayerDataFlow.first()

        provideContent {
            GlanceTheme(colors = WidgetThemeColors) {
                if (prayerData != null) {
                    HijriDateContent(prayerData)
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
fun HijriDateContent(prayerData: PrayerData) {
    val hijriDateStr = prayerData.hijriDate
    
    // Format is usually something like "12 Rabi' al-Awwal 1445"
    // Let's split it nicely if possible
    val parts = hijriDateStr.split(" ")
    val day = parts.firstOrNull() ?: ""
    val year = parts.lastOrNull() ?: ""
    val month = if (parts.size > 2) parts.subList(1, parts.size - 1).joinToString(" ") else ""

    Column(
        modifier = GlanceModifier.fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (parts.size >= 3) {
            Text(
                text = day,
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = month,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = year,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            )
        } else {
            // Fallback if the format is not exactly what we expect
            Text(
                text = hijriDateStr,
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
