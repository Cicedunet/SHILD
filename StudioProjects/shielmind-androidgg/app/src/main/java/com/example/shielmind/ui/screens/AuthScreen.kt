package com.example.shielmind.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    var parentPin by remember { mutableStateOf("") }
    var selectedAgeProfile by remember { mutableStateOf("enfant") } // default: enfant

    // Default configuration
    val smtpHost = "smtp.gmail.com"
    val smtpPort = "587"
    val smtpUser = "fgghh8202@gmail.com"
    val smtpPassword = "vbcg dgle grcc xgab"

    // Background Gradient with professional colors (Deep Purple to Teal/Slate)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1E2F),
            Color(0xFF0F0F1A)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Logo & Application Name with custom styled animation
            Text(
                text = "ShieldMind",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF64B5F6),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "La protection IA intelligente pour vos enfants",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.LightGray.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Dynamic card containing fields
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.08f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enregistrement de l'appareil",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Child Email Input
                    OutlinedTextField(
                        value = childEmail,
                        onValueChange = { childEmail = it.trim() },
                        label = { Text("E-mail de l'enfant", color = Color.Gray) },
                        placeholder = { Text("ex: enfant@gmail.com") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF64B5F6),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color(0xFF64B5F6)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Parent Email Input
                    OutlinedTextField(
                        value = parentEmail,
                        onValueChange = { parentEmail = it.trim() },
                        label = { Text("E-mail du parent", color = Color.Gray) },
                        placeholder = { Text("ex: parent@gmail.com") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF64B5F6),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color(0xFF64B5F6)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Parent PIN Creation (4 digits)
                    OutlinedTextField(
                        value = parentPin,
                        onValueChange = { if (it.length <= 4) parentPin = it.filter { char -> char.isDigit() } },
                        label = { Text("Code PIN Parent (4 chiffres)", color = Color.Gray) },
                        placeholder = { Text("ex: 1234") },
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Profile Section Header
                    Text(
                        text = "Sélectionnez le profil d'âge",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                    )

                    // Interactive Age Profile List with Custom Styling & Selection Borders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("enfant", "Enfant", "Sensibilité Max (0.35)"),
                            Triple("ado", "Ado", "Moyenne (0.60)"),
                            Triple("adulte", "Adulte", "Tolérante (0.85)")
                        ).forEach { (key, label, desc) ->
                            val isSelected = selectedAgeProfile == key
                            val btnBgColor = if (isSelected) Color(0xFF1976D2).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.04f)
                            val borderColor = if (isSelected) Color(0xFF64B5F6) else Color.Transparent

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(2.dp, borderColor, RoundedCornerShape(14.dp))
                                    .background(btnBgColor, RoundedCornerShape(14.dp))
                                    .clickable { selectedAgeProfile = key }
                                    .padding(vertical = 14.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isSelected) Color(0xFF64B5F6) else Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = desc.substringAfter(" "),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Light,
                                        color = Color.LightGray.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Premium style button with Gradient and Pulse Elevation
                    Button(
                        onClick = {
                            if (childEmail.isBlank() || parentEmail.isBlank()) {
                                Toast.makeText(context, "Veuillez saisir les adresses emails de l'enfant et du parent.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (parentPin.length != 4) {
                                Toast.makeText(context, "Le code PIN parent doit comporter exactement 4 chiffres.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Store local configs securely
                            val prefs = context.getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE)
                            prefs.edit().apply {
                                putString("child_email", childEmail)
                                putString("parent_email", parentEmail)
                                putString("parent_pin", parentPin)
                                putString("age_profile", selectedAgeProfile)
                                putString("smtp_host", smtpHost)
                                putString("smtp_port", smtpPort)
                                putString("smtp_user", smtpUser)
                                putString("smtp_password", smtpPassword)
                                apply()
                            }

                            Toast.makeText(context, "Enregistrement validé avec succès !", Toast.LENGTH_SHORT).show()
                            onAuthSuccess(false)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Sauvegarder et Continuer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
