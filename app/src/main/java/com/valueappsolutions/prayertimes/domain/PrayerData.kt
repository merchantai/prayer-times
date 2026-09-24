package com.valueappsolutions.prayertimes.domain

import kotlinx.serialization.Serializable

@Serializable
data class PrayerData(
    val fajr: String,
    val sunrise: String,
    val ishraq: String,
    val chasht: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val tahajjud: String,
    val fajrLocal: String = "",
    val sunriseLocal: String = "",
    val ishraqLocal: String = "",
    val chashtLocal: String = "",
    val dhuhrLocal: String = "",
    val asrLocal: String = "",
    val maghribLocal: String = "",
    val ishaLocal: String = "",
    val tahajjudLocal: String = "",
    val isLocalTimeDifferent: Boolean = false,
    val destinationTimezoneId: String = "",
    val hijriDate: String,
    val hijriDay: Int,
    val currentDate: String,
    val isOfflineFallback: Boolean,
    val areaName: String = "",
    val cityName: String = "",
    val moonFraction: Double = 0.0,
    val lastUpdatedTime: Long = 0L,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val calculationMethod: Int = 2,
    val asrMadhab: Int = 1,
    val hijriOffset: Int = 0,
    val tahajjudMethod: Int = 1
)
