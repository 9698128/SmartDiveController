package com.divecontroller.data.api

import com.divecontroller.data.api.models.*
import retrofit2.Response
import retrofit2.http.*

interface DiveApiService {

    @GET("api/dive/sites")
    suspend fun getAllSites(): Response<List<DiveSite>>

    @GET("api/dive/sites/{siteId}/current")
    suspend fun getCurrentConditions(@Path("siteId") siteId: String): Response<SensorData>

    @GET("api/dive/sites/{siteId}/history")
    suspend fun getHistoryData(
        @Path("siteId") siteId: String,
        @Query("hours") hours: Int = 24
    ): Response<List<SensorData>>

    @GET("api/dive/alerts/active")
    suspend fun getActiveAlerts(): Response<List<Alert>>

    @GET("api/dive/sites/{siteId}/alerts")
    suspend fun getSiteAlerts(@Path("siteId") siteId: String): Response<List<Alert>>
}