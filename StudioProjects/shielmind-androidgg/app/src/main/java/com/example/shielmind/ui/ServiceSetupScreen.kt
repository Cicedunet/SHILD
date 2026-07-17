package com.example.shielmind.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var selectedTab by remember { mutableStateOf(0) } // 0: Protection, 1: Historique

    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        isDeviceAdmin = DeviceAdminHelper.isDeviceAdmin(context)
    }

    // Refresh state when tab is opened
    LaunchedEffect(selectedTab) {
        isAccessibilityEnabled = AccessibilityHelper.isAccessibilityServiceEnabled(context)
        isDeviceAdmin = DeviceAdminHelper.isDeviceAdmin(context)
    }

    val prefs = remember { context.getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE) }
    var ageProfile by remember { mutableStateOf(prefs.getString("age_profile", "enfant") ?: "enfant") }
    val childEmail = prefs.getString("child_email", "Enfant") ?: "Enfant"

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1E2F),
            Color(0xFF0F0F1A)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ShieldMind Dashboard", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141423),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        isAccessibilityEnabled = AccessibilityHelper.isAccessibilityServiceEnabled(context)
                        isDeviceAdmin = DeviceAdminHelper.isDeviceAdmin(context)
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF64B5F6))
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Customized Tab Row with clean glassmorphism style
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF141423),
                    contentColor = Color(0xFF64B5F6),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF64B5F6)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Protection Active", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Historique IA", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.List, contentDescription = null) }
                    )
                }

                Crossfade(targetState = selectedTab, label = "tabCrossfade") { tab ->
                    when (tab) {
                        0 -> ProtectionTab(
                            context = context,
                            isAccessibilityEnabled = isAccessibilityEnabled,
                            isDeviceAdmin = isDeviceAdmin,
                            ageProfile = ageProfile,
                            childEmail = childEmail,
                            onAgeProfileChanged = { newProfile ->
                                ageProfile = newProfile
                                prefs.edit().putString("age_profile", newProfile).apply()
                                Toast.makeText(context, "Seuil de sensibilité mis à jour !", Toast.LENGTH_SHORT).show()
                            },
                            deviceAdminLauncher = deviceAdminLauncher
                        )
                        1 -> HistoryTab(context = context)
                    }
                }
            }
        }
    }
}

@Composable
fun ProtectionTab(
    context: Context,
    isAccessibilityEnabled: Boolean,
    isDeviceAdmin: Boolean,
    ageProfile: String,
    childEmail: String,
    onAgeProfileChanged: (String) -> Unit,
    deviceAdminLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pulsing Radar Protection Animation
        Box(
            modifier = Modifier
                .size(160.dp)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            // Pulse wave
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color(0xFF64B5F6).copy(alpha = alpha))
            )

            // Inner solid shield circle
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1E88E5), Color(0xFF1565C0))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Protection Active de l'Enfant",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = childEmail,
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Sensibilité / Age Profile segment directly inline
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Niveau de filtrage (Profil d'âge)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "enfant" to "Enfant (Stricte)",
                        "ado" to "Ado",
                        "adulte" to "Adulte (Souple)"
                    ).forEach { (key, label) ->
                        val isSelected = ageProfile == key
                        val btnBg = if (isSelected) Color(0xFF1E88E5) else Color.White.copy(alpha = 0.04f)
                        val textColor = if (isSelected) Color.White else Color.Gray

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(btnBg)
                                .clickable { onAgeProfileChanged(key) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Steps cards
        SetupStepCard(
            title = "Service d'Accessibilité",
            description = "Indispensable pour analyser en temps réel l'écran de votre enfant et fermer le contenu inapproprié.",
            isActive = isAccessibilityEnabled,
            icon = Icons.Default.Info,
            onAction = { AccessibilityHelper.openAccessibilitySettings(context) },
            actionLabel = "Activer le service"
        )

        Spacer(modifier = Modifier.height(14.dp))

        SetupStepCard(
            title = "Protection Administrateur",
            description = "Empêche l'enfant d'arrêter ou de désinstaller cette application de sécurité.",
            isActive = isDeviceAdmin,
            icon = Icons.Default.Lock,
            onAction = {
                val intent = DeviceAdminHelper.buildDeviceAdminIntent(context)
                deviceAdminLauncher.launch(intent)
            },
            actionLabel = "Sécuriser l'appareil"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Simulation Block test button
        Button(
            onClick = {
                val intent = android.content.Intent(context, com.example.shielmind.MainActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("BLOCK_REASON", "Simulation d'un blocage de contenu inapproprié")
                }
                context.startActivity(intent)
            },
            colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Tester une simulation de blocage", fontSize = 12.sp)
        }
    }
}

@Composable
fun HistoryTab(context: Context) {
    val prefs = remember { context.getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE) }
    var historyList by remember {
        mutableStateOf(
            prefs.getStringSet("block_history", emptySet())
                ?.toList()
                ?.sortedByDescending { it.substringBefore("|") }
                ?: emptyList()
        )
    }

    if (historyList.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF81C784),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tout est propre !",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Aucun blocage de contenu inapproprié n'a été signalé pour l'instant.",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Blocages Récents (${historyList.size})",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "Effacer tout",
                    color = Color(0xFFEF5350),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        prefs.edit().putStringSet("block_history", emptySet()).apply()
                        historyList = emptyList()
                        Toast.makeText(context, "Historique vidé !", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList) { item ->
                    val parts = item.split("|")
                    if (parts.size >= 3) {
                        val timestamp = parts[0]
                        val app = parts[1]
                        val snippet = parts[2]
                        val score = if (parts.size > 3) parts[3] else "1.00"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.04f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = app.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF64B5F6)
                                    )
                                    Text(
                                        text = timestamp,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = snippet,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color(0xFFEF5350).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFEF5350),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Toxicity score: $score",
                                        color = Color(0xFFEF5350),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
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

@Composable
fun SetupStepCard(
    title: String,
    description: String,
    isActive: Boolean,
    icon: ImageVector,
    onAction: () -> Unit,
    actionLabel: String
) {
    val scale by animateFloatAsState(targetValue = if (isActive) 1.0f else 0.98f, label = "cardScale")
    val bgColor = if (isActive) Color(0xFF81C784).copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f)
    val borderColor = if (isActive) Color(0xFF81C784).copy(alpha = 0.4f) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isActive) Color(0xFF81C784) else Color(0xFF1E88E5).copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.CheckCircle else icon,
                            contentDescription = null,
                            tint = if (isActive) Color.White else Color(0xFF64B5F6)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (isActive) Color(0xFF81C784) else Color.White
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.LightGray.copy(alpha = 0.7f),
                lineHeight = 17.sp
            )

            if (!isActive) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                ) {
                    Text(actionLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
