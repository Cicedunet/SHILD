package com.example.shielmind

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.shielmind.service.FirebaseSyncManager
import com.example.shielmind.ui.ServiceSetupScreen
import com.example.shielmind.ui.screens.EducationalBlockScreen
import com.example.shielmind.ui.theme.ShielMindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockReason = intent.getStringExtra("BLOCK_REASON")

        enableEdgeToEdge()
        setContent {
            ShielMindTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (blockReason != null) {
                        EducationalBlockScreen(
                            onUnblockRequest = {
                                Toast.makeText(this@MainActivity, "Demande envoyée au parent", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        ServiceSetupScreen()
                    }
                }
            }
        }

        // Écoute des décisions parentales à distance
        FirebaseSyncManager.listenForParentDecision("child_user_123") { approvedText ->
            Toast.makeText(this, "Parent a autorisé : $approvedText", Toast.LENGTH_LONG).show()
            // Fermer l'activité de blocage
            finish()
        }
    }
}
