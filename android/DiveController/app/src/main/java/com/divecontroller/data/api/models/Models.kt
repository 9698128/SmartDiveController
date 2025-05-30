package com.divecontroller.data.api.models

import com.google.gson.annotations.SerializedName

data class DiveSite(
    @SerializedName("site_id") val siteId: String,
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("depth_category") val depthCategory: String,
    @SerializedName("status") val status: String,
    @SerializedName("last_update") val lastUpdate: String
)

data class SensorData(
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("site_id") val siteId: String,
    @SerializedName("sensor_id") val sensorId: String,
    @SerializedName("depth") val depth: String,
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("current_speed") val currentSpeed: Double,
    @SerializedName("current_direction") val currentDirection: Int,
    @SerializedName("visibility") val visibility: Double,
    @SerializedName("luminosity") val luminosity: Double,
    @SerializedName("battery_level") val batteryLevel: Double
)

data class Alert(
    @SerializedName("type") val type: String,
    @SerializedName("level") val level: String, // warning, critical
    @SerializedName("message") val message: String,
    @SerializedName("value") val value: Double,
    @SerializedName("threshold") val threshold: Double,
    @SerializedName("site_id") val siteId: String,
    @SerializedName("timestamp") val timestamp: String
)

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T?,
    @SerializedName("error") val error: String?
)