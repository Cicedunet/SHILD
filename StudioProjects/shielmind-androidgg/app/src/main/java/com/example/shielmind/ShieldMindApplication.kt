package com.example.shielmind

import android.app.Application

class ShieldMindApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // No Firebase initialization, fully local email-based system
    }
}
