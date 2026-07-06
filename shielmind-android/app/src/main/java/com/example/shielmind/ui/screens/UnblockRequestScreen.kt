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
fun UnblockRequestScreen(
    onSendRequest: (String) -> Unit,
    onCancel: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Demande de déblocage",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Expliquez à votre parent pourquoi vous avez besoin d'accéder à ce contenu."
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Raison") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onSendRequest(reason) },
            modifier = Modifier.fillMaxWidth(),
            enabled = reason.isNotBlank()
        ) {
            Text("Envoyer la demande")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Annuler")
        }
    }
}
