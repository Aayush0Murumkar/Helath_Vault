package com.example

import android.app.Application
import android.util.Log
import com.example.backend.firebase.FirebaseInitializer

/**
 * Health Vault Application entry point.
 * Initializes Firebase Firestore, Auth, and Storage during application startup.
 */
class HealthVaultApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i("HealthVaultApp", "Application onCreate: Initializing Firebase services...")
        FirebaseInitializer.initialize(this)
    }
}
