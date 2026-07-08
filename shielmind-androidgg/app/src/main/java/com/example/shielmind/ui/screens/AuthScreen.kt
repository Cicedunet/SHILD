package com.example.shielmind.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthScreen(
    onAuthSuccess: (Boolean) -> Unit // Boolean: true if parent, false if child
) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isParent by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLogin) "Connexion" else "Inscription",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isParent, onClick = { isParent = true })
            Text("Parent")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = !isParent, onClick = { isParent = false })
            Text("Enfant")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onAuthSuccess(isParent) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLogin) "Se connecter" else "S'inscrire")
        }

        TextButton(onClick = { isLogin = !isLogin }) {
            Text(if (isLogin) "Pas encore de compte ? S'inscrire" else "Déjà un compte ? Se connecter")
        }
    }
}
