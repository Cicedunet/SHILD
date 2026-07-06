package com.example.shielmind.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ParentDashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Tableau de Bord Parent",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Statistics Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Alertes", "12", Color.Red, modifier = Modifier.weight(1f))
            StatCard("Temps", "2h 15", Color.Blue, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Historique des alertes",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val alerts = listOf(
            AlertItem("Contenu violent détecté", "WhatsApp", "Il y a 5 min"),
            AlertItem("Tentative d'accès site adulte", "Chrome", "Il y a 1h"),
            AlertItem("Propos haineux", "Instagram", "Hier")
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(alerts) { alert ->
                AlertCard(alert)
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 14.sp)
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AlertCard(alert: AlertItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = alert.title, fontWeight = FontWeight.Bold)
                Text(text = "App: ${alert.app}", fontSize = 12.sp)
                Text(text = alert.time, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

data class AlertItem(val title: String, val app: String, val time: String)
