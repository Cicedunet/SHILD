package com.example.shielmind.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EducationalBlockScreen(
    reason: String?,
    onUnblockRequest: () -> Unit,
    onSafeExit: () -> Unit = {},
    onParentOverrideSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val isTimeExceeded = reason == "TEMPS_EPUISE"

    // PIN dialog state for time exceed override
    var showOverrideDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Background gradient: Orange/Red for standard warning, Deep Violet/Dark Blue for Screen Time Limit
    val bgGradient = if (isTimeExceeded) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF2E0854), Color(0xFF14052C))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFB71C1C), Color(0xFFD32F2F))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isTimeExceeded) Icons.Default.Lock else Icons.Default.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(80.dp)
                    .animateContentSize()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isTimeExceeded) "Temps Épuisé" else "Contenu Sensible",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isTimeExceeded) Color(0xFF4A148C) else Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isTimeExceeded) {
                            "La limite quotidienne d'utilisation de l'appareil a été atteinte."
                        } else {
                            "ShieldMind a détecté un contenu qui pourrait être inapproprié."
                        },
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Divider(color = Color.LightGray.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isTimeExceeded) {
                        Button(
                            onClick = { showOverrideDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C))
                        ) {
                            Text("Ajouter du temps (Parent)", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = onSafeExit,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("Retourner en sécurité", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isTimeExceeded) {
                        TextButton(
                            onClick = onSafeExit,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Fermer et quitter", color = Color.Gray)
                        }
                    } else {
                        TextButton(
                            onClick = onUnblockRequest,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Demander l'autorisation au parent", color = Color.Gray)
                        }
                    }
                }
            }
        }

        // Parent Override Dialog for adding time
        if (showOverrideDialog) {
            val prefs = remember { context.getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE) }
            val correctPin = prefs.getString("parent_pin", "0000") ?: "0000"

            AlertDialog(
                onDismissRequest = { showOverrideDialog = false },
                title = { Text("Code PIN Parental", fontWeight = FontWeight.Bold, color = Color.White) },
                containerColor = Color(0xFF1E1E2F),
                text = {
                    Column {
                        Text("Saisissez votre code PIN de sécurité parent pour accorder +15 minutes de temps d'utilisation :", color = Color.LightGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = { if (it.length <= 4) enteredPin = it.filter { char -> char.isDigit() } },
                            placeholder = { Text("ex: 1234", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF64B5F6),
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = Color(0xFF64B5F6)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (enteredPin == correctPin) {
                                // Add 15 minutes to today's screen limit
                                val currentLimit = prefs.getInt("screen_time_limit_minutes", 30)
                                prefs.edit().putInt("screen_time_limit_minutes", currentLimit + 15).apply()
                                showOverrideDialog = false
                                enteredPin = ""
                                Toast.makeText(context, "15 minutes supplémentaires accordées !", Toast.LENGTH_SHORT).show()
                                onParentOverrideSuccess()
                            } else {
                                Toast.makeText(context, "Code PIN incorrect !", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C))
                    ) {
                        Text("Accorder 15 min", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOverrideDialog = false }) {
                        Text("Annuler", color = Color.LightGray)
                    }
                }
            )
        }
    }
}
