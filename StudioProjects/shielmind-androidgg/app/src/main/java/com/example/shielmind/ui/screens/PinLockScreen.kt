package com.example.shielmind.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinLockScreen(
    onCorrectPin: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE) }
    val correctPin = prefs.getString("parent_pin", "0000") ?: "0000"

    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val shakeOffset = remember { Animatable(0f) }

    // Shaking animation logic on error
    LaunchedEffect(isError) {
        if (isError) {
            repeat(4) {
                shakeOffset.animateTo(15f, animationSpec = tween(50, easing = LinearEasing))
                shakeOffset.animateTo(-15f, animationSpec = tween(50, easing = LinearEasing))
            }
            shakeOffset.animateTo(0f, animationSpec = tween(50, easing = LinearEasing))
            enteredPin = ""
            isError = false
        }
    }

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
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(x = shakeOffset.value.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFF64B5F6),
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Espace Parent Sécurisé",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Veuillez saisir votre code PIN de sécurité",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Pin Dots Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    val dotColor = if (isFilled) Color(0xFF64B5F6) else Color.White.copy(alpha = 0.2f)
                    val dotScale by animateFloatAsState(targetValue = if (isFilled) 1.2f else 1.0f, label = "dot")

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    )
                }
            }

            // Numeric Keypad Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("effacer", "0", "")
                )

                for (row in keys) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        for (key in row) {
                            if (key.isEmpty()) {
                                Box(modifier = Modifier.weight(1f))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .clickable {
                                            if (key == "effacer") {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                }
                                            } else {
                                                if (enteredPin.length < 4) {
                                                    enteredPin += key
                                                    if (enteredPin.length == 4) {
                                                        if (enteredPin == correctPin) {
                                                            onCorrectPin()
                                                        } else {
                                                            isError = true
                                                            Toast.makeText(context, "Code PIN incorrect !", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "effacer") {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
