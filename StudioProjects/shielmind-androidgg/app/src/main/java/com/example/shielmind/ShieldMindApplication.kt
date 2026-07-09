package com.example.shielmind

import android.app.Application
import com.google.firebase.FirebaseApp

class ShieldMindApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}
