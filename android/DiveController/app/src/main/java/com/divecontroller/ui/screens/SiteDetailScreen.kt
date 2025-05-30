package com.divecontroller.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.divecontroller.ui.components.*
import com.divecontroller.ui.viewmodels.SiteDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteDetailScreen(
    siteId: String,
    onNavigateBack: () -> Unit,
    viewModel: SiteDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Carica dati quando la schermata si apre
    LaunchedEffect(siteId) {
        viewModel.loadSiteData(siteId)
    }

    // Auto-refresh ogni 30 secondi
    LaunchedEffect(siteId) {
        while (true) {
            kotlinx.coroutines.delay(30000) // 30 secondi
            viewModel.refreshData(siteId)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header sito con pull-to-refresh
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🌊 ${siteId.replace("_", " ").uppercase()}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Text(
                                text = "Ultima lettura: ${uiState.lastUpdate}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        // Pulsante refresh
                        IconButton(
                            onClick = { viewModel.refreshData(siteId) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Aggiorna",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Mostra errore se presente
                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9800) // Arancione
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Condizioni Attuali",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Mostra loading o dati
        when {
            uiState.isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            uiState.currentData != null -> {
                val currentData = uiState.currentData!!

                item {
                    // Griglia sensori attuali
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CurrentSensorCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Thermostat,
                            label = "Temperatura",
                            value = "${currentData.temperature}°C",
                            color = Color(0xFF2196F3)
                        )

                        CurrentSensorCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Air,
                            label = "Corrente",
                            value = "${String.format("%.1f", currentData.currentSpeed)}m/s",
                            color = Color(0xFF00BCD4)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CurrentSensorCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Visibility,
                            label = "Visibilità",
                            value = "${String.format("%.1f", currentData.visibility)}m",
                            color = Color(0xFF4CAF50)
                        )

                        CurrentSensorCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.WbSunny,
                            label = "Luminosità",
                            value = "${String.format("%.0f", currentData.luminosity)} lux",
                            color = Color(0xFFFF9800)
                        )
                    }
                }

                item {
                    // Batteria
                    BatteryCard(
                        batteryLevel = currentData.batteryLevel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Dettagli aggiuntivi
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Dettagli Sensore",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            DetailRow("ID Sensore", currentData.sensorId)
                            DetailRow("Profondità", currentData.depth.uppercase())
                            DetailRow("Direzione corrente", "${currentData.currentDirection}°")
                            DetailRow("Timestamp", currentData.timestamp.substring(0, 19))
                        }
                    }
                }
            }

            else -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "❌ Nessun dato disponibile",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Storico Ultime 24h",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            // Placeholder grafico
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Grafico Temperature",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Implementazione in corso...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CurrentSensorCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BatteryCard(
    batteryLevel: Double,
    modifier: Modifier = Modifier
) {
    val batteryColor = when {
        batteryLevel > 50 -> Color(0xFF4CAF50) // Verde
        batteryLevel > 20 -> Color(0xFFFF9800) // Arancione
        else -> Color(0xFFF44336) // Rosso
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = batteryColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = when {
                        batteryLevel > 75 -> Icons.Default.BatteryFull
                        batteryLevel > 50 -> Icons.Default.Battery6Bar
                        batteryLevel > 25 -> Icons.Default.Battery3Bar
                        else -> Icons.Default.Battery1Bar
                    },
                    contentDescription = "Batteria",
                    tint = batteryColor,
                    modifier = Modifier.size(32.dp)
                )

                Column {
                    Text(
                        text = "Livello Batteria",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Sensore autonomo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${String.format("%.1f", batteryLevel)}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = batteryColor
            )
        }
    }
}