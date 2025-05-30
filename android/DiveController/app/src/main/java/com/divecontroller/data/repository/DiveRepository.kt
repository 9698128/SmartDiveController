package com.divecontroller.data.repository

import com.divecontroller.data.api.DiveApiService
import com.divecontroller.data.api.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class DiveRepository(private val apiService: DiveApiService) {

    fun getAllSites(): Flow<Result<List<DiveSite>>> = flow {
        try {
            val response = apiService.getAllSites()
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Errore API: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getCurrentConditions(siteId: String): Flow<Result<SensorData>> = flow {
        try {
            val response = apiService.getCurrentConditions(siteId)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Errore API: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getHistoryData(siteId: String, hours: Int = 24): Flow<Result<List<SensorData>>> = flow {
        try {
            val response = apiService.getHistoryData(siteId, hours)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Errore API: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getActiveAlerts(): Flow<Result<List<Alert>>> = flow {
        try {
            val response = apiService.getActiveAlerts()
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Errore API: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}