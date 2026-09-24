package com.valueappsolutions.prayertimes.domain.alarms

import java.time.LocalDateTime

data class AlarmItem(
    val id: Int, // e.g., 1 for Fajr, 2 for Dhuhr, etc.
    val time: LocalDateTime,
    val prayerName: String,
    val type: Int = 0 // 0 = Prayer, 1 = Silent Start, 2 = Silent End
)

interface AlarmScheduler {
    fun schedule(item: AlarmItem)
    fun cancel(item: AlarmItem)
}
