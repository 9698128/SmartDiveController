package com.divecontroller.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AlertsScreen() {
    val mockAlerts = remember { mockAlertsList() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "🚨 Avvisi Sistema",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${mockAlerts.size} avvisi attivi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (mockAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50), // Verde
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Tutto OK!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50) // Verde
                        )

                        Text(
                            text = "Nessun avviso attivo al momento",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(mockAlerts) { alert ->
                AlertDetailCard(alert = alert)
            }
        }
    }
}

@Composable
fun AlertDetailCard(alert: Map<String, Any>) {
    val level = alert["level"] as String
    val backgroundColor = when (level) {
        "critical" -> Color(0xFFF44336).copy(alpha = 0.1f) // Rosso
        "warning" -> Color(0xFFFF9800).copy(alpha = 0.1f)  // Arancione (era Orange)
        else -> Color(0xFF9E9E9E).copy(alpha = 0.1f) // Grigio
    }

    val iconColor = when (level) {
        "critical" -> Color(0xFFF44336) // Rosso
        "warning" -> Color(0xFFFF9800)  // Arancione (era Orange)
        else -> Color(0xFF9E9E9E) // Grigio
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (level == "critical") Icons.Default.Error else Icons.Default.Warning,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = alert["message"] as String,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = iconColor
                    )

                    Surface(
                        color = iconColor.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = level.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = iconColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Sito: ${alert["site_id"]}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Valore: ${alert["value"]} (Soglia: ${alert["threshold"]})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Ora: ${alert["time"]}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun mockAlertsList(): List<Map<String, Any>> {
    return listOf(
        mapOf(
            "level" to "warning",
            "message" to "Corrente forte rilevata",
            "site_id" to "capo_vaticano",
            "value" to "1.8 m/s",
            "threshold" to "1.5",
            "time" to "12:30"
        ),
        mapOf(
            "level" to "critical",
            "message" to "Batteria sensore scarica",
            "site_id" to "tropea_reef",
            "value" to "15%",
            "threshold" to "20",
            "time" to "11:45"
        ),
        mapOf(
            "level" to "warning",
            "message" to "Visibilità ridotta",
            "site_id" to "stromboli_east",
            "value" to "4.2m",
            "threshold" to "5",
            "time" to "10:15"
        )
    )
}