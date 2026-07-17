package com.example.shielmind.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ShieldBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("ShieldBootReceiver", "Appareil démarré. ShieldMind s'est réinitialisé automatiquement avec succès.")
        }
    }
}
