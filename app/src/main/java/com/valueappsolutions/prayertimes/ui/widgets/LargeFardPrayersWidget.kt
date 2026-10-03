package com.valueappsolutions.prayertimes.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Box
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
import androidx.glance.appwidget.cornerRadius
import com.valueappsolutions.prayertimes.MainActivity

import androidx.glance.appwidget.SizeMode
import androidx.glance.LocalSize

class LargeFardPrayersWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val userPrefs = UserPreferencesRepository(context)

        provideContent {
            val prayerData by userPrefs.cachedPrayerDataFlow.collectAsState(initial = null)
            val userSettings by userPrefs.userSettingsFlow.collectAsState(initial = null)
            val is24HourFormat = userSettings?.is24HourFormat ?: false

            GlanceTheme(colors = WidgetThemeColors) {
                if (prayerData != null) {
                    LargeFardPrayersContent(prayerData!!, is24HourFormat)
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
fun LargeFardPrayersContent(prayerData: PrayerData, is24HourFormat: Boolean) {
    val size = LocalSize.current
    val scale = (size.height.value / 250f).coerceIn(0.7f, 1.5f)

    val nameSize = (16f * scale).sp
    val timeSize = (18f * scale).sp

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

    Column(
        modifier = GlanceModifier.fillMaxSize()
            .background(GlanceTheme.colors.background)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp)
    ) {
        fardPrayers.forEach { (name, timeStr) ->
            val isCurrent = name == currentPrayer
            val formattedTime = WidgetUtils.formatDisplayTimeStr(timeStr, is24HourFormat)
            
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .padding(vertical = 2.dp)
                    .background(if (isCurrent) GlanceTheme.colors.primary else GlanceTheme.colors.background)
                    .cornerRadius(12.dp)
                    .padding(1.dp)
            ) {
                Row(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(if (isCurrent) GlanceTheme.colors.primaryContainer else GlanceTheme.colors.surface)
                        .cornerRadius(11.dp)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = nameSize
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = formattedTime,
                        style = TextStyle(
                            color = if (isCurrent) GlanceTheme.colors.primary else GlanceTheme.colors.onSurface,
                            fontSize = timeSize,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
