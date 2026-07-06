package com.example.shielmind.ui

// ════════════════════════════════════════════════════════
// FICHIER : app/src/main/java/com/example/shielmind/ui/ServiceSetupScreen.kt
// VERSION MISE À JOUR avec la section Device Administrator
// ════════════════════════════════════════════════════════

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shielmind.accessibility.AccessibilityHelper
import com.example.shielmind.admin.DeviceAdminHelper

@Composable
fun ServiceSetupScreen() {

    val context = LocalContext.current

    // ── État 1 : Service d'Accessibilité ──────────────────────────────────────
    var isAccessibilityEnabled by remember {
        mutableStateOf(AccessibilityHelper.isAccessibilityServiceEnabled(context))
    }

    // ── État 2 : Device Administrator ─────────────────────────────────────────
    var isDeviceAdmin by remember {
        mutableStateOf(DeviceAdminHelper.isDeviceAdmin(context))
    }

    // Launcher Compose pour lancer l'écran d'activation Device Admin
    // et récupérer le résultat (RESULT_OK = l'utilisateur a accepté)
    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Après retour du dialog système, on vérifie si c'est bien activé
        if (result.resultCode == Activity.RESULT_OK) {
            isDeviceAdmin = DeviceAdminHelper.isDeviceAdmin(context)
        }
        // On re-vérifie même si annulé, pour afficher l'état correct
        isDeviceAdmin = DeviceAdminHelper.isDeviceAdmin(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Configuration ShieldMind",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── SECTION 1 : Accessibilité ──────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isAccessibilityEnabled)
                    Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isAccessibilityEnabled)
                        "✅ Service d'accessibilité actif"
                    else
                        "⚠️ Service d'accessibilité inactif",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Permet à ShieldMind d'analyser les textes affichés à l'écran.",
                    style = MaterialTheme.typography.bodySmall
                )
                if (!isAccessibilityEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        AccessibilityHelper.openAccessibilitySettings(context)
                    }) {
                        Text("Activer le service")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── SECTION 2 : Device Administrator ──────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isDeviceAdmin)
                    Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isDeviceAdmin)
                        "✅ Protection anti-désinstallation active"
                    else
                        "⚠️ Protection anti-désinstallation inactive",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isDeviceAdmin)
                        "L'enfant ne peut pas désinstaller ShieldMind sans le code PIN parent."
                    else
                        "Sans cette protection, l'enfant peut supprimer ShieldMind librement.",
                    style = MaterialTheme.typography.bodySmall
                )
                if (!isDeviceAdmin) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        // Lance le dialog système Android pour activer le mode admin
                        val intent = DeviceAdminHelper.buildDeviceAdminIntent(context)
                        deviceAdminLauncher.launch(intent)
                    }) {
                        Text("Activer la protection")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Bouton de rafraîchissement ─────────────────────────────────────────
        OutlinedButton(onClick = {
            isAccessibilityEnabled = AccessibilityHelper.isAccessibilityServiceEnabled(context)
            isDeviceAdmin = DeviceAdminHelper.isDeviceAdmin(context)
        }) {
            Text("Vérifier à nouveau")
        }

        // ── Résumé global ──────────────────────────────────────────────────────
        if (isAccessibilityEnabled && isDeviceAdmin) {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
            ) {
                Text(
                    text = "🛡️ ShieldMind est entièrement configuré et actif !",
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}