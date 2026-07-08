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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private var blockReasonState = mutableStateOf<String?>(null)

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        blockReasonState.value = intent?.getStringExtra("BLOCK_REASON")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        blockReasonState.value = intent.getStringExtra("BLOCK_REASON")
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        enableEdgeToEdge()
        setContent {
            ShielMindTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val blockReason by blockReasonState
                    var currentScreen by remember { mutableStateOf(if (auth.currentUser == null) "auth" else "loading") }
                    var userRole by remember { mutableStateOf<String?>(null) }

                    // Fetch user role if already logged in
                    LaunchedEffect(Unit) {
                        if (auth.currentUser != null) {
                            db.collection("users").document(auth.currentUser!!.uid).get()
                                .addOnSuccessListener { doc ->
                                    userRole = doc.getString("role")
                                    currentScreen = if (userRole == "parent") "dashboard" else "setup"
                                }
                        }
                    }

                    if (blockReason != null) {
                        EducationalBlockScreen(
                            onUnblockRequest = {
                                Toast.makeText(this@MainActivity, "Demande envoyée au parent", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        when (currentScreen) {
                            "auth" -> AuthScreen(onAuthSuccess = { isParent ->
                                currentScreen = if (isParent) "pairing" else "setup"
                            })
                            "pairing" -> PairingScreen(onPairingComplete = {
                                currentScreen = "dashboard"
                            })
                            "setup" -> ServiceSetupScreen()
                            "dashboard" -> ParentDashboardScreen()
                            "loading" -> { /* Simple loader */ }
                        }
                    }
                }
            }
        }

        // Écoute des décisions parentales à distance si c'est un enfant
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                FirebaseSyncManager.listenForParentDecision(user.uid) { approvedText ->
                    Toast.makeText(this, "Parent a autorisé : $approvedText", Toast.LENGTH_LONG).show()
                    blockReasonState.value = null
                }
            }
        }
    }
}
