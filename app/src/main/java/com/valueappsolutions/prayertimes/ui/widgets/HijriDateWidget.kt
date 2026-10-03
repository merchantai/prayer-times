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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.valueappsolutions.prayertimes.domain.PrayerData
import com.valueappsolutions.prayertimes.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import com.valueappsolutions.prayertimes.MainActivity

import androidx.glance.appwidget.SizeMode
import androidx.glance.LocalSize
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.width

class HijriDateWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val userPrefs = UserPreferencesRepository(context)

        provideContent {
            val prayerData by userPrefs.cachedPrayerDataFlow.collectAsState(initial = null)

            GlanceTheme(colors = WidgetThemeColors) {
                if (prayerData != null) {
                    HijriDateContent(prayerData!!)
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
fun HijriDateContent(prayerData: PrayerData) {
    val size = LocalSize.current
    val scale = (size.width.value / 120f).coerceIn(0.7f, 1.5f)

    val daySize = (32f * scale).sp // Increased slightly since it's side-by-side
    val monthSize = (16f * scale).sp
    val yearSize = (18f * scale).sp
    val fallbackSize = (18f * scale).sp

    val hijriDateStr = prayerData.hijriDate
    
    // Format is usually something like "12 Rabi' al-Awwal 1445"
    // Let's split it nicely if possible
    val parts = hijriDateStr.split(" ")
    val day = parts.firstOrNull() ?: ""
    val year = parts.lastOrNull() ?: ""
    val month = if (parts.size > 2) parts.subList(1, parts.size - 1).joinToString(" ") else ""

    Row(
        modifier = GlanceModifier.fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .clickable(actionStartActivity<MainActivity>())
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (parts.size >= 3) {
            Text(
                text = day,
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = daySize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp * scale))
            Column(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = month,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = monthSize,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start
                    )
                )
                Text(
                    text = year,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = yearSize,
                        textAlign = TextAlign.Start
                    )
                )
            }
        } else {
            // Fallback if the format is not exactly what we expect
            Text(
                text = hijriDateStr,
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = fallbackSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
