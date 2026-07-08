package com.example.shielmind.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shielmind.service.FirebaseSyncManager
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PairingScreen(
    onPairingComplete: () -> Unit
) {
    val context = LocalContext.current
    var childId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val parentId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

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
            label = { Text("ID Enfant (ex: CHILD_123)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (childId.isBlank()) return@Button
                    isLoading = true
                    FirebaseSyncManager.linkChildToParent(parentId, childId) { success ->
                        isLoading = false
                        if (success) {
                            Toast.makeText(context, "Lien réussi !", Toast.LENGTH_SHORT).show()
                            onPairingComplete()
                        } else {
                            Toast.makeText(context, "Erreur lors du lien.", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = childId.isNotBlank()
            ) {
                Text("Lier maintenant")
            }
        }
    }
}
