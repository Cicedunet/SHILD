package com.example.shielmind

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

        val blockReasonFromIntent = intent.getStringExtra("BLOCK_REASON")
        if (blockReasonFromIntent != null) {
            blockReasonState.value = blockReasonFromIntent
        }

        enableEdgeToEdge()
        setContent {
            ShielMindTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val blockReason by blockReasonState

                    // Retrieve stored emails to determine initial screen
                    val prefs = remember { getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE) }
                    var childEmail by remember { mutableStateOf(prefs.getString("child_email", null)) }
                    var parentEmail by remember { mutableStateOf(prefs.getString("parent_email", null)) }

                    var currentScreen by remember {
                        mutableStateOf(
                            if (childEmail.isNullOrBlank() || parentEmail.isNullOrBlank()) "auth" else "setup"
                        )
                    }

                    if (blockReason != null) {
                        EducationalBlockScreen(
                            onUnblockRequest = {
                                Toast.makeText(this@MainActivity, "Demande envoyée au parent", Toast.LENGTH_SHORT).show()
                            },
                            onSafeExit = {
                                // Minimize app / go to home screen
                                moveTaskToBack(true)
                            }
                        )
                    } else {
                        when (currentScreen) {
                            "auth" -> AuthScreen(onAuthSuccess = { _ ->
                                // Refresh cached email state
                                childEmail = prefs.getString("child_email", null)
                                parentEmail = prefs.getString("parent_email", null)
                                currentScreen = "setup"
                            })
                            "setup" -> ServiceSetupScreen()
                            "loading" -> { /* Simple loader */ }
                        }
                    }
                }
            }
        }
    }
}
