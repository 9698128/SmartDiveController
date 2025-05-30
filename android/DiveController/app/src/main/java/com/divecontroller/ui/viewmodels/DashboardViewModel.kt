package com.divecontroller.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divecontroller.data.api.models.*
import com.divecontroller.utils.NetworkModule
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val sites: List<DiveSite> = emptyList(),
    val currentConditions: Map<String, SensorData> = emptyMap(),
    val alerts: List<Alert> = emptyList(),
    val error: String? = null,
    val mqttConnected: Boolean = false,
    val databaseConnected: Boolean = false
)

class DashboardViewModel : ViewModel() {

    private val repository = NetworkModule.repository

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Try to load real data from API
                repository.getAllSites().collect { result ->
                    result.fold(
                        onSuccess = { sites ->
                            _uiState.value = _uiState.value.copy(
                                sites = sites,
                                isLoading = false,
                                mqttConnected = true,
                                databaseConnected = true
                            )

                            // Load current conditions for each site
                            loadCurrentConditions(sites.map { it.siteId })
                        },
                        onFailure = { error ->
                            // If API fails, use mock data as fallback
                            val mockSites = listOf(
                                DiveSite(
                                    siteId = "capo_vaticano",
                                    name = "Capo Vaticano",
                                    latitude = 38.6878,
                                    longitude = 15.8742,
                                    depthCategory = "shallow",
                                    status = "online",
                                    lastUpdate = "2025-05-30T12:34:56Z"
                                ),
                                DiveSite(
                                    siteId = "tropea_reef",
                                    name = "Tropea Reef",
                                    latitude = 38.6767,
                                    longitude = 15.8989,
                                    depthCategory = "deep",
                                    status = "warning",
                                    lastUpdate = "2025-05-30T12:30:15Z"
                                ),
                                DiveSite(
                                    siteId = "stromboli_east",
                                    name = "Stromboli East",
                                    latitude = 38.7891,
                                    longitude = 15.2134,
                                    depthCategory = "surface",
                                    status = "online",
                                    lastUpdate = "2025-05-30T12:35:22Z"
                                )
                            )

                            _uiState.value = _uiState.value.copy(
                                sites = mockSites,
                                isLoading = false,
                                error = "Usando dati offline - Controlla connessione: ${error.message}",
                                mqttConnected = false,
                                databaseConnected = false
                            )

                            // Load mock current conditions
                            loadMockCurrentConditions()
                        }
                    )
                }

                // Load active alerts
                loadActiveAlerts()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Errore di connessione: ${e.message}",
                    isLoading = false,
                    mqttConnected = false,
                    databaseConnected = false
                )
            }
        }
    }

    private fun loadCurrentConditions(siteIds: List<String>) {
        viewModelScope.launch {
            val currentConditions = mutableMapOf<String, SensorData>()

            siteIds.forEach { siteId ->
                repository.getCurrentConditions(siteId).collect { result ->
                    result.fold(
                        onSuccess = { data ->
                            currentConditions[siteId] = data
                            _uiState.value = _uiState.value.copy(
                                currentConditions = currentConditions.toMap()
                            )
                        },
                        onFailure = {
                            // Use mock data for this site
                            currentConditions[siteId] = generateMockSensorData(siteId)
                            _uiState.value = _uiState.value.copy(
                                currentConditions = currentConditions.toMap()
                            )
                        }
                    )
                }
            }
        }
    }

    private fun loadMockCurrentConditions() {
        val mockConditions = mapOf(
            "capo_vaticano" to SensorData(
                timestamp = "2025-05-30T12:34:56Z",
                siteId = "capo_vaticano",
                sensorId = "capo_vaticano_sensor_01",
                depth = "shallow",
                temperature = 19.2,
                currentSpeed = 0.8,
                currentDirection = 45,
                visibility = 22.0,
                luminosity = 850.0,
                batteryLevel = 78.0
            ),
            "tropea_reef" to SensorData(
                timestamp = "2025-05-30T12:30:15Z",
                siteId = "tropea_reef",
                sensorId = "tropea_reef_sensor_01",
                depth = "deep",
                temperature = 16.5,
                currentSpeed = 1.2,
                currentDirection = 120,
                visibility = 28.0,
                luminosity = 65.0,
                batteryLevel = 45.0
            ),
            "stromboli_east" to SensorData(
                timestamp = "2025-05-30T12:32:18Z",
                siteId = "stromboli_east",
                sensorId = "stromboli_east_sensor_01",
                depth = "surface",
                temperature = 20.1,
                currentSpeed = 0.3,
                currentDirection = 200,
                visibility = 15.0,
                luminosity = 1200.0,
                batteryLevel = 92.0
            )
        )

        _uiState.value = _uiState.value.copy(
            currentConditions = mockConditions
        )
    }

    private fun generateMockSensorData(siteId: String): SensorData {
        return SensorData(
            timestamp = "2025-05-30T12:34:56Z",
            siteId = siteId,
            sensorId = "${siteId}_sensor_01",
            depth = "shallow",
            temperature = 15.0 + (Math.random() * 10),
            currentSpeed = Math.random() * 2,
            currentDirection = (Math.random() * 360).toInt(),
            visibility = 10.0 + (Math.random() * 20),
            luminosity = Math.random() * 1000,
            batteryLevel = 20.0 + (Math.random() * 80)
        )
    }

    private fun loadActiveAlerts() {
        viewModelScope.launch {
            repository.getActiveAlerts().collect { result ->
                result.fold(
                    onSuccess = { alerts ->
                        _uiState.value = _uiState.value.copy(alerts = alerts)
                    },
                    onFailure = {
                        // Use mock alerts as fallback
                        val mockAlerts = listOf(
                            Alert(
                                type = "current",
                                level = "warning",
                                message = "Corrente forte rilevata",
                                value = 1.8,
                                threshold = 1.5,
                                siteId = "capo_vaticano",
                                timestamp = "2025-05-30T12:30:00Z"
                            ),
                            Alert(
                                type = "battery",
                                level = "critical",
                                message = "Batteria sensore scarica",
                                value = 15.0,
                                threshold = 20.0,
                                siteId = "tropea_reef",
                                timestamp = "2025-05-30T11:45:00Z"
                            )
                        )
                        _uiState.value = _uiState.value.copy(alerts = mockAlerts)
                    }
                )
            }
        }
    }
}
