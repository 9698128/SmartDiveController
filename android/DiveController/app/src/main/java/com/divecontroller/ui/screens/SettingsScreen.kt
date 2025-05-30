package com.divecontroller.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoRefresh by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "⚙️ Impostazioni",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            SettingsSection(title = "Notifiche") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifiche Push",
                    description = "Ricevi avvisi per alert critici",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
        }

        item {
            SettingsSection(title = "Aggiornamento Dati") {
                SettingsSwitchItem(
                    icon = Icons.Default.Refresh,
                    title = "Aggiornamento Automatico",
                    description = "Aggiorna dati ogni 30 secondi",
                    checked = autoRefresh,
                    onCheckedChange = { autoRefresh = it }
                )
            }
        }

        item {
            SettingsSection(title = "Aspetto") {
                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "Tema Scuro",
                    description = "Attiva modalità scura",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }
        }

        item {
            SettingsSection(title = "Connessione") {
                SettingsActionItem(
                    icon = Icons.Default.NetworkCheck,
                    title = "Stato Connessione",
                    description = "Verifica connessione ai servizi",
                    onClick = { /* Implementa test connessione */ }
                )

                SettingsActionItem(
                    icon = Icons.Default.Settings,
                    title = "Configurazione Server",
                    description = "Modifica indirizzo server",
                    onClick = { /* Implementa configurazione */ }
                )
            }
        }

        item {
            SettingsSection(title = "Informazioni") {
                SettingsActionItem(
                    icon = Icons.Default.Info,
                    title = "Versione App",
                    description = "1.0.0 (Build 1)",
                    onClick = { }
                )

                SettingsActionItem(
                    icon = Icons.Default.Code,
                    title = "Sviluppato con",
                    description = "Jetpack Compose, Node-RED, InfluxDB",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}