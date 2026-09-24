package com.valueappsolutions.prayertimes.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AlAdhanResponse(
    val code: Int,
    val status: String,
    val data: AlAdhanData
)

@Serializable
data class AlAdhanData(
    val timings: Timings,
    val date: DateInfo,
    val meta: MetaInfo
)

@Serializable
data class MetaInfo(
    val timezone: String
)

@Serializable
data class Timings(
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Sunset: String,
    val Maghrib: String,
    val Isha: String,
    val Imsak: String? = null,
    val Midnight: String? = null
)

@Serializable
data class DateInfo(
    val readable: String,
    val timestamp: String,
    val hijri: HijriDate
)

@Serializable
data class HijriDate(
    val date: String,
    val format: String,
    val day: String,
    val month: HijriMonth,
    val year: String
)

@Serializable
data class HijriMonth(
    val number: Int,
    val en: String,
    val ar: String
)
