package com.example.shielmind.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shielmind.service.FirebaseSyncManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen() {
    var alerts by remember { mutableStateOf(listOf<AlertItem>()) }

    // Simulation de chargement depuis Firestore
    LaunchedEffect(Unit) {
        // En prod, on écouterait db.collection("alerts").whereEqualTo("parentId", currentParentId)
        alerts = listOf(
            AlertItem("id1", "Contenu violent détecté", "WhatsApp", "Il y a 5 min"),
            AlertItem("id2", "Tentative d'accès site adulte", "Chrome", "Il y a 1h"),
            AlertItem("id3", "Propos haineux", "Instagram", "Hier")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("ShieldMind Parent") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Statistics
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Alertes", alerts.size.toString(), Color(0xFFD32F2F), modifier = Modifier.weight(1f))
                StatCard("Enfants", "1", Color(0xFF1976D2), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Dernières Alertes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(alerts) { alert ->
                    AlertCard(
                        alert = alert,
                        onApprove = {
                            // Action: Autoriser le contenu
                            alerts = alerts.filter { it.id != alert.id }
                        },
                        onBlock = {
                            // Action: Maintenir le blocage
                            alerts = alerts.filter { it.id != alert.id }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun AlertCard(alert: AlertItem, onApprove: () -> Unit, onBlock: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = alert.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Source: ${alert.app}", style = MaterialTheme.typography.bodySmall)
                }
                Text(text = alert.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onBlock, colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ignorer")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Autoriser")
                }
            }
        }
    }
}

data class AlertItem(val id: String, val title: String, val app: String, val time: String)
