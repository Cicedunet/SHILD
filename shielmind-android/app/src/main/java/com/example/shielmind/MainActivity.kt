package com.example.shielmind

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.shielmind.service.FirebaseSyncManager
import com.example.shielmind.ui.ServiceSetupScreen
import com.example.shielmind.ui.screens.*
import com.example.shielmind.ui.theme.ShielMindTheme

class MainActivity : ComponentActivity() {

    private var blockReasonState = mutableStateOf<String?>(null)

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        blockReasonState.value = intent?.getStringExtra("BLOCK_REASON")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        blockReasonState.value = intent.getStringExtra("BLOCK_REASON")

        enableEdgeToEdge()
        setContent {
            ShielMindTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val blockReason by blockReasonState
                    var currentScreen by remember { mutableStateOf("auth") }
                    var isParentRole by remember { mutableStateOf(false) }

                    if (blockReason != null) {
                        EducationalBlockScreen(
                            onUnblockRequest = {
                                Toast.makeText(this@MainActivity, "Demande envoyée au parent", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        when (currentScreen) {
                            "auth" -> AuthScreen(onAuthSuccess = { isParent ->
                                isParentRole = isParent
                                currentScreen = if (isParent) "pairing" else "setup"
                            })
                            "pairing" -> PairingScreen(onPairingComplete = {
                                currentScreen = "dashboard"
                            })
                            "setup" -> ServiceSetupScreen()
                            "dashboard" -> ParentDashboardScreen()
                        }
                    }
                }
            }
        }

        // Écoute des décisions parentales à distance
        FirebaseSyncManager.listenForParentDecision("child_user_123") { approvedText ->
            Toast.makeText(this, "Parent a autorisé : $approvedText", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
