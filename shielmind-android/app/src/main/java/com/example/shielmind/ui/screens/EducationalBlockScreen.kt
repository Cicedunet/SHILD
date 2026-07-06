package com.example.shielmind.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EducationalBlockScreen(
    onUnblockRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF44336)) // Red background for warning
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Contenu Bloqué",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ShieldMind a détecté du contenu potentiellement inapproprié selon vos paramètres de protection.",
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Pourquoi ce blocage ?",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "L'IA a identifié des termes ou contextes liés à la violence ou à des contenus inappropriés.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { /* Go back */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Retour")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onUnblockRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Demander le déblocage")
                }
            }
        }
    }
}
