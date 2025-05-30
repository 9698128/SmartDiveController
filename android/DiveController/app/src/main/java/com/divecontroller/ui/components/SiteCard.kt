package com.divecontroller.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.divecontroller.data.api.models.DiveSite
import com.divecontroller.data.api.models.SensorData

@Composable
fun SiteCard(
    site: DiveSite,
    currentData: SensorData?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header sito
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = site.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Profondità: ${site.depthCategory}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status indicator
                StatusBadge(status = site.status)
            }

            if (currentData != null) {
                Spacer(modifier = Modifier.height(12.dp))

                // Dati sensori
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SensorValue(
                        icon = Icons.Default.Thermostat,
                        label = "Temp",
                        value = "${currentData.temperature.toInt()}°C",
                        color = getTemperatureColor(currentData.temperature)
                    )

                    SensorValue(
                        icon = Icons.Default.Air,
                        label = "Corrente",
                        value = "${String.format("%.1f", currentData.currentSpeed)}m/s",
                        color = getCurrentColor(currentData.currentSpeed)
                    )

                    SensorValue(
                        icon = Icons.Default.Visibility,
                        label = "Visibilità",
                        value = "${currentData.visibility.toInt()}m",
                        color = getVisibilityColor(currentData.visibility)
                    )

                    SensorValue(
                        icon = Icons.Default.BatteryFull,
                        label = "Batteria",
                        value = "${currentData.batteryLevel.toInt()}%",
                        color = getBatteryColor(currentData.batteryLevel)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dati non disponibili",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "online" -> Color(0xFF4CAF50)   // Verde
        "warning" -> Color(0xFFFF9800)  // Arancione (era Orange)
        "critical" -> Color(0xFFF44336) // Rosso
        else -> Color(0xFF9E9E9E)       // Grigio
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SensorValue(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Funzioni helper per colori - FIX: Sostituiti Color.Orange con Color(0xFFFF9800)
fun getTemperatureColor(temp: Double): Color {
    return when {
        temp < 12 -> Color(0xFFF44336)  // Rosso
        temp < 18 -> Color(0xFFFF9800)  // Arancione
        else -> Color(0xFF4CAF50)       // Verde
    }
}

fun getCurrentColor(speed: Double): Color {
    return when {
        speed > 1.5 -> Color(0xFFF44336)  // Rosso
        speed > 1.0 -> Color(0xFFFF9800)  // Arancione
        else -> Color(0xFF4CAF50)         // Verde
    }
}

fun getVisibilityColor(visibility: Double): Color {
    return when {
        visibility < 5 -> Color(0xFFF44336)   // Rosso
        visibility < 10 -> Color(0xFFFF9800)  // Arancione
        else -> Color(0xFF4CAF50)             // Verde
    }
}

fun getBatteryColor(battery: Double): Color {
    return when {
        battery < 20 -> Color(0xFFF44336)  // Rosso
        battery < 50 -> Color(0xFFFF9800)  // Arancione
        else -> Color(0xFF4CAF50)          // Verde
    }
}