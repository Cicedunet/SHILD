package com.example.shielmind.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PairingScreen(
    onPairingComplete: () -> Unit
) {
    var childId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Lier un compte enfant",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Entrez l'identifiant unique affiché sur le téléphone de votre enfant."
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = childId,
            onValueChange = { childId = it },
            label = { Text("ID Enfant") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onPairingComplete() },
            modifier = Modifier.fillMaxWidth(),
            enabled = childId.isNotBlank()
        ) {
            Text("Lier maintenant")
        }
    }
}
