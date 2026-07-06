package com.example.shielmind.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

class ShieldFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("ShieldFCM", "From: ${remoteMessage.from}")

        // Handle data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d("ShieldFCM", "Message data payload: " + remoteMessage.data)
        }

        // Handle notification payload
        remoteMessage.notification?.let {
            Log.d("ShieldFCM", "Message Notification Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("ShieldFCM", "Refreshed token: $token")
        // Send token to backend or save locally
    }
}
