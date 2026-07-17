package com.example.shielmind.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthScreen(
    onAuthSuccess: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var childEmail by remember { mutableStateOf("") }
    var parentEmail by remember { mutableStateOf("") }

    // Hardcoded SMTP Settings (already pre-configured in the app)
    val smtpHost = "smtp.gmail.com"
    val smtpPort = "587"
    val smtpUser = "fgghh8202@gmail.com"
    val smtpPassword = "vbcg dgle grcc xgab"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ShieldMind",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Enregistrement de l'appareil",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = childEmail,
            onValueChange = { childEmail = it.trim() },
            label = { Text("Email Enfant") },
            placeholder = { Text("ex: enfant@gmail.com") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = parentEmail,
            onValueChange = { parentEmail = it.trim() },
            label = { Text("Email Parent") },
            placeholder = { Text("ex: parent@gmail.com") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (childEmail.isBlank() || parentEmail.isBlank()) {
                    Toast.makeText(context, "Veuillez saisir les adresses emails de l'enfant et du parent.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Sauvegarde locale dans SharedPreferences
                val prefs = context.getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("child_email", childEmail)
                    putString("parent_email", parentEmail)
                    putString("smtp_host", smtpHost)
                    putString("smtp_port", smtpPort)
                    putString("smtp_user", smtpUser)
                    putString("smtp_password", smtpPassword)
                    apply()
                }

                Toast.makeText(context, "Configuration sauvegardée avec succès !", Toast.LENGTH_SHORT).show()
                // Redirection directe vers la configuration du service
                onAuthSuccess(false)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Sauvegarder et Continuer")
        }
    }
}
