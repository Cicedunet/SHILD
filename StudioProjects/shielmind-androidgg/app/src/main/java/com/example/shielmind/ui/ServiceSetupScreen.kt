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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shielmind.accessibility.AccessibilityHelper
import com.example.shielmind.admin.DeviceAdminHelper
import com.example.shielmind.service.LocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceSetupScreen() {
    val context = LocalContext.current
    var isAccessibilityEnabled by remember { mutableStateOf(AccessibilityHelper.isAccessibilityServiceEnabled(context)) }
    var isDeviceAdmin by remember { mutableStateOf(DeviceAdminHelper.isDeviceAdmin(context)) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Protection, 1: Contrôle & Géo, 2: Historique

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
                // Customized Tab Row with clean glassmorphism style (3 Tabs)
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
                        text = { Text("Protection", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Contrôle & Géo", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Historique IA", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(20.dp)) }
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
                        1 -> ControlTab(context = context)
                        2 -> HistoryTab(context = context)
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
    val prefs = remember { context.getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE) }

    // PIN Modification state
    var showPinChangeDialog by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf("") }
    var currentStoredPin by remember { mutableStateOf(prefs.getString("parent_pin", "0000") ?: "0000") }
    var isPinVisible by remember { mutableStateOf(false) }

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

        // PIN Modification Section
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Code PIN Parental",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "Code PIN : " + if (isPinVisible) currentStoredPin else "••••",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPinVisible) "Masquer" else "Afficher",
                            fontSize = 11.sp,
                            color = Color(0xFF64B5F6),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { isPinVisible = !isPinVisible }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
                Button(
                    onClick = { showPinChangeDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                ) {
                    Text("Modifier", fontSize = 12.sp)
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

        // Dialog for PIN changing
        if (showPinChangeDialog) {
            AlertDialog(
                onDismissRequest = { showPinChangeDialog = false },
                title = { Text("Modifier le code PIN", fontWeight = FontWeight.Bold, color = Color.White) },
                containerColor = Color(0xFF1E1E2F),
                text = {
                    Column {
                        Text("Saisissez un nouveau code PIN parental à 4 chiffres :", color = Color.LightGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { if (it.length <= 4) newPin = it.filter { char -> char.isDigit() } },
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
                            if (newPin.length == 4) {
                                prefs.edit().putString("parent_pin", newPin).apply()
                                currentStoredPin = newPin
                                showPinChangeDialog = false
                                newPin = ""
                                Toast.makeText(context, "Code PIN mis à jour avec succès !", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Le code PIN doit comporter exactement 4 chiffres.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                    ) {
                        Text("Enregistrer", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinChangeDialog = false }) {
                        Text("Annuler", color = Color.LightGray)
                    }
                }
            )
        }
    }
}

@Composable
fun ControlTab(context: Context) {
    val prefs = remember { context.getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE) }
    val scrollState = rememberScrollState()

    // 1. Screen Time limit states
    var limitMinutes by remember { mutableStateOf(prefs.getInt("screen_time_limit_minutes", 30)) }
    var usedSeconds by remember { mutableStateOf(prefs.getInt("screen_time_used_seconds", 0)) }

    // Periodically refresh the actual elapsed time on the dashboard while this tab is open
    LaunchedEffect(Unit) {
        while (true) {
            usedSeconds = prefs.getInt("screen_time_used_seconds", 0)
            limitMinutes = prefs.getInt("screen_time_limit_minutes", 30)
            kotlinx.coroutines.delay(1000)
        }
    }

    val usedMinutes = usedSeconds / 60
    val totalLimitSeconds = limitMinutes * 60
    val progressFraction = if (totalLimitSeconds > 0) usedSeconds.toFloat() / totalLimitSeconds.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // Interpolated Color for progress: Green to Yellow to Red
    val progressColor = when {
        progressFraction <= 0.5f -> Color(0xFF81C784)
        progressFraction <= 0.8f -> Color(0xFFFFB74D)
        else -> Color(0xFFE57373)
    }

    // 2. Geolocation states
    var hasLocationPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var currentLocation by remember { mutableStateOf<android.location.Location?>(LocationHelper.getCurrentLocation(context)) }
    var isRefreshingLocation by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            currentLocation = LocationHelper.getCurrentLocation(context)
        }
    }

    // Rotation animation for Geolocation refresh button
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(isRefreshingLocation) {
        if (isRefreshingLocation) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rotation.animateTo(0f, tween(300))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- SCREEN TIME MANAGEMENT SECTION ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow, // clock representation
                        contentDescription = null,
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Limite de Temps d'Écran",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Beautiful Circular Progress Dashboard
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(130.dp)
                ) {
                    CircularProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.size(120.dp),
                        color = progressColor,
                        strokeWidth = 8.dp,
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$usedMinutes m",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "sur $limitMinutes min",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Ajuster la limite de temps quotidienne",
                    fontSize = 13.sp,
                    color = Color.LightGray.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("5 min", fontSize = 11.sp, color = Color.Gray)
                    Slider(
                        value = limitMinutes.toFloat(),
                        onValueChange = {
                            limitMinutes = it.toInt()
                            prefs.edit().putInt("screen_time_limit_minutes", limitMinutes).apply()
                        },
                        valueRange = 5f..180f,
                        steps = 34,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF1E88E5),
                            activeTrackColor = Color(0xFF1E88E5),
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    Text("180 min", fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reset option
                TextButton(
                    onClick = {
                        prefs.edit().putInt("screen_time_used_seconds", 0).apply()
                        usedSeconds = 0
                        Toast.makeText(context, "Compteur de temps réinitialisé !", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF64B5F6))
                ) {
                    Text("Réinitialiser le compteur du jour", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- GEOLOCATION REAL FEATURE ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Géo-localisation Temps Réel",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    if (hasLocationPermission) {
                        IconButton(
                            onClick = {
                                isRefreshingLocation = true
                                LocationHelper.requestFreshLocationUpdate(context) { location ->
                                    currentLocation = location
                                    isRefreshingLocation = false
                                    Toast.makeText(context, "Position mise à jour !", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.rotate(rotation.value)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir", tint = Color(0xFF81C784))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!hasLocationPermission) {
                    Text(
                        text = "L'accès à la position GPS de l'appareil de l'enfant est requis pour afficher sa position en temps réel sur la carte parent.",
                        fontSize = 12.sp,
                        color = Color.LightGray.copy(alpha = 0.8f),
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))
                    ) {
                        Text("Accorder l'accès position GPS", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                } else {
                    val location = currentLocation
                    if (location != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Latitude", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    String.format(java.util.Locale.US, "%.6f", location.latitude),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Longitude", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    String.format(java.util.Locale.US, "%.6f", location.longitude),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Précision", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    "${location.accuracy.toInt()} mètres",
                                    fontSize = 12.sp,
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}")
                                )
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                        ) {
                            Text("Afficher l'enfant sur Google Maps", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF81C784), modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Recherche du signal GPS réel...",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
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
