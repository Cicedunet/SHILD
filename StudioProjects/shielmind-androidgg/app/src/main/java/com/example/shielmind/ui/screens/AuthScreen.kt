package com.example.shielmind.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AuthScreen(
    onAuthSuccess: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isParent by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
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
        Text(text = if (isLogin) "Connexion" else "Créer un compte", color = Color.Gray)

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

        if (!isLogin) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = isParent, onClick = { isParent = true })
                Text("Parent")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = !isParent, onClick = { isParent = false })
                Text("Enfant")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) return@Button
                    isLoading = true
                    if (isLogin) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener { result ->
                                // Fetch role from Firestore
                                db.collection("users").document(result.user!!.uid).get()
                                    .addOnSuccessListener { doc ->
                                        if (doc.exists()) {
                                            val role = doc.getString("role") ?: "parent"
                                            onAuthSuccess(role == "parent")
                                        } else {
                                            // Si le document n'existe pas, on le crée par défaut
                                            val role = "parent"
                                            val newUser = hashMapOf(
                                                "email" to email,
                                                "role" to role,
                                                "uid" to result.user!!.uid
                                            )
                                            db.collection("users").document(result.user!!.uid).set(newUser)
                                            onAuthSuccess(true)
                                        }
                                    }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Erreur: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener { result ->
                                val uid = result.user!!.uid
                                val user = hashMapOf(
                                    "email" to email,
                                    "role" to if (isParent) "parent" else "enfant",
                                    "uid" to uid,
                                    "createdAt" to System.currentTimeMillis()
                                )
                                db.collection("users").document(uid).set(user)
                                    .addOnSuccessListener {
                                        Log.d("AuthScreen", "Profil utilisateur créé pour $uid")
                                        onAuthSuccess(isParent)
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        Toast.makeText(context, "Erreur Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Erreur: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLogin) "Se connecter" else "S'inscrire")
            }

            TextButton(onClick = { isLogin = !isLogin }) {
                Text(if (isLogin) "Pas encore de compte ? S'inscrire" else "Déjà un compte ? Se connecter")
            }
        }
    }
}
