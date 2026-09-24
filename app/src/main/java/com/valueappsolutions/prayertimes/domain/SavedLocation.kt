package com.valueappsolutions.prayertimes.domain

import kotlinx.serialization.Serializable

@Serializable
data class SavedLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String = ""
)
