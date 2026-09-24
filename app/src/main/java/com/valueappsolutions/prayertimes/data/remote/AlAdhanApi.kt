package com.valueappsolutions.prayertimes.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface AlAdhanApi {
    @GET("v1/timings")
    suspend fun getTimings(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int, // e.g. 2 for ISNA, 3 for MWL
        @Query("shafaq") shafaq: String = "general",
        @Query("tune") tune: String? = null,
        @Query("school") school: Int = 0, // 0 for Shafi, 1 for Hanafi
        @Query("hijriAdjustment") adjustment: Int = 0
    ): AlAdhanResponse
}
