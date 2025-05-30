package com.divecontroller.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divecontroller.data.api.models.SensorData
import com.divecontroller.utils.NetworkModule
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class SiteDetailUiState(
    val isLoading: Boolean = false,
    val currentData: SensorData? = null,
    val error: String? = null,
    val lastUpdate: String = ""
)

class SiteDetailViewModel : ViewModel() {

    private val repository = NetworkModule.repository

    private val _uiState = MutableStateFlow(SiteDetailUiState())
    val uiState: StateFlow<SiteDetailUiState> = _uiState.asStateFlow()

    fun loadSiteData(siteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                repository.getCurrentConditions(siteId).collect { result ->
                    result.fold(
                        onSuccess = { data ->
                            _uiState.value = _uiState.value.copy(
                                currentData = data,
                                isLoading = false,
                                lastUpdate = formatTimestamp(data.timestamp),
                                error = null
                            )
                        },
                        onFailure = { error ->
                            // Fallback ai dati mock se l'API fallisce
                            val mockData = generateMockData(siteId)
                            _uiState.value = _uiState.value.copy(
                                currentData = mockData,
                                isLoading = false,
                                lastUpdate = "Dati offline",
                                error = "Connessione API fallita: ${error.message}"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                val mockData = generateMockData(siteId)
                _uiState.value = _uiState.value.copy(
                    currentData = mockData,
                    isLoading = false,
                    error = "Errore di rete: ${e.message}"
                )
            }
        }
    }

    fun refreshData(siteId: String) {
        loadSiteData(siteId)
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            // Converte ISO timestamp in formato leggibile
            val time = timestamp.substring(11, 16) // HH:MM
            val date = timestamp.substring(8, 10) + "/" +
                    timestamp.substring(5, 7) + "/" +
                    timestamp.substring(0, 4)
            "$time - $date"
        } catch (e: Exception) {
            timestamp
        }
    }

    private fun generateMockData(siteId: String): SensorData {
        // Dati mock diversi per ogni sito (fallback)
        val mockConfigs = mapOf(
            "capo_vaticano" to Triple(19.2, 0.8, 78.0),
            "tropea_reef" to Triple(16.5, 1.2, 45.0),
            "stromboli_east" to Triple(20.1, 0.3, 92.0)
        )

        val (temp, current, battery) = mockConfigs[siteId] ?: Triple(18.0, 0.5, 50.0)

        return SensorData(
            timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date()),
            siteId = siteId,
            sensorId = "${siteId}_sensor_01",
            depth = "shallow",
            temperature = temp,
            currentSpeed = current,
            currentDirection = 45,
            visibility = 22.0,
            luminosity = 850.0,
            batteryLevel = battery
        )
    }
}