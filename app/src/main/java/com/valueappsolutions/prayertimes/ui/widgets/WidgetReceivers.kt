package com.valueappsolutions.prayertimes.ui.widgets

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class CurrentNextPrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = CurrentNextPrayerWidget()
}

class LargeFardPrayersWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = LargeFardPrayersWidget()
}

class HorizontalFardPrayersWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = HorizontalFardPrayersWidget()
}

class HijriDateWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = HijriDateWidget()
}
