package com.divecontroller.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.divecontroller.data.api.models.Alert

@Composable
fun AlertBanner(
    alerts: List<Alert>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alert",
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "Avvisi Attivi (${alerts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alerts) { alert ->
                    AlertChip(alert = alert)
                }
            }
        }
    }
}

@Composable
fun AlertChip(alert: Alert) {
    val backgroundColor = when (alert.level) {
        "critical" -> Color(0xFFF44336).copy(alpha = 0.1f) // Rosso
        "warning" -> Color(0xFFFF9800).copy(alpha = 0.1f)  // Arancione (era Orange)
        else -> Color(0xFF9E9E9E).copy(alpha = 0.1f)       // Grigio
    }

    val textColor = when (alert.level) {
        "critical" -> Color(0xFFF44336) // Rosso
        "warning" -> Color(0xFFFF9800)  // Arancione (era Orange)
        else -> Color(0xFF9E9E9E)       // Grigio
    }

    val icon = when (alert.type) {
        "temperature" -> Icons.Default.Thermostat
        "current" -> Icons.Default.Air
        "visibility" -> Icons.Default.Visibility
        "battery" -> Icons.Default.Battery1Bar
        else -> Icons.Default.Warning
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = "${alert.siteId}: ${alert.message}",
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}