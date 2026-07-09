package com.example.shielmind.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shielmind.accessibility.AccessibilityHelper
import com.example.shielmind.admin.DeviceAdminHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceSetupScreen() {
    val context = LocalContext.current
    var isAccessibilityEnabled by remember { mutableStateOf(AccessibilityHelper.isAccessibilityServiceEnabled(context)) }
    var isDeviceAdmin by remember { mutableStateOf(DeviceAdminHelper.isDeviceAdmin(context)) }

    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        isDeviceAdmin = DeviceAdminHelper.isDeviceAdmin(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuration ShieldMind", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        isAccessibilityEnabled = AccessibilityHelper.isAccessibilityServiceEnabled(context)
                        isDeviceAdmin = DeviceAdminHelper.isDeviceAdmin(context)
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ID de jumelage : CHILD_8842",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Protégez votre enfant en 2 étapes",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            SetupStepCard(
                title = "Service d'Accessibilité",
                description = "Nécessaire pour analyser le texte à l'écran en temps réel.",
                isActive = isAccessibilityEnabled,
                icon = Icons.Default.Info,
                onAction = { AccessibilityHelper.openAccessibilitySettings(context) },
                actionLabel = "Activer"
            )

            Spacer(modifier = Modifier.height(16.dp))

            SetupStepCard(
                title = "Protection Administrateur",
                description = "Empêche l'enfant de désinstaller l'application.",
                isActive = isDeviceAdmin,
                icon = Icons.Default.Lock,
                onAction = {
                    val intent = DeviceAdminHelper.buildDeviceAdminIntent(context)
                    deviceAdminLauncher.launch(intent)
                },
                actionLabel = "Sécuriser"
            )

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(
                visible = isAccessibilityEnabled && isDeviceAdmin,
                enter = fadeIn() + expandVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tout est prêt ! L'application fonctionne maintenant en arrière-plan.",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SetupStepCard(
    title: String,
    description: String,
    isActive: Boolean,
    icon: ImageVector,
    onAction: () -> Unit,
    actionLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFF1F8E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isActive) Color(0xFF81C784) else Color(0xFFE3F2FD)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.CheckCircle else icon,
                            contentDescription = null,
                            tint = if (isActive) Color.White else Color(0xFF1976D2)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            if (!isActive) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}
