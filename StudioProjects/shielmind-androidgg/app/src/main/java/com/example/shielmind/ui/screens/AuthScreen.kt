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
    var smtpHost by remember { mutableStateOf("smtp.gmail.com") }
    var smtpPort by remember { mutableStateOf("587") }
    var smtpUser by remember { mutableStateOf("") }
    var smtpPassword by remember { mutableStateOf("") }

    // Pre-fill smtpUser with parentEmail as a helpful default
    LaunchedEffect(parentEmail) {
        if (smtpUser.isBlank() || smtpUser == parentEmail.substringBefore("@") /* dynamic helper */) {
            smtpUser = parentEmail
        }
    }

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
            text = "Enregistrement & Configuration SMTP",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 24.dp)
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

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Paramètres de messagerie (SMTP)",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            text = "Nécessaire pour envoyer des alertes par mail au parent de manière autonome sans serveur.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(vertical = 4.dp).align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = smtpHost,
            onValueChange = { smtpHost = it.trim() },
            label = { Text("Serveur SMTP") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = smtpPort,
            onValueChange = { smtpPort = it.trim() },
            label = { Text("Port SMTP") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = smtpUser,
            onValueChange = { smtpUser = it.trim() },
            label = { Text("Utilisateur SMTP (Expéditeur)") },
            placeholder = { Text("ex: parent@gmail.com") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = smtpPassword,
            onValueChange = { smtpPassword = it },
            label = { Text("Mot de passe d'application SMTP") },
            placeholder = { Text("Mot de passe d'application ou d'accès") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (childEmail.isBlank() || parentEmail.isBlank()) {
                    Toast.makeText(context, "Veuillez saisir les adresses emails de l'enfant et du parent.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (smtpUser.isBlank() || smtpPassword.isBlank()) {
                    Toast.makeText(context, "Veuillez configurer les identifiants SMTP pour l'envoi de mails.", Toast.LENGTH_SHORT).show()
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
