package com.example.backend.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage

/**
 * Centralized Firebase initialization and configuration manager.
 * Safely initializes FirebaseApp, Cloud Firestore, FirebaseAuth, and FirebaseStorage
 * during application startup with fail-safe error handling for GMS/emulator runtime environments.
 */
object FirebaseInitializer {

    private const val TAG = "FirebaseInitializer"

    @Volatile
    private var isInitialized = false

    var firestoreInstance: FirebaseFirestore? = null
        private set

    var authInstance: FirebaseAuth? = null
        private set

    var storageInstance: FirebaseStorage? = null
        private set

    val firestore: FirebaseFirestore?
        get() = try {
            firestoreInstance ?: FirebaseFirestore.getInstance().also { firestoreInstance = it }
        } catch (e: Throwable) {
            Log.w(TAG, "Firestore instance unavailable: ${e.message}")
            null
        }

    val auth: FirebaseAuth?
        get() = try {
            authInstance ?: FirebaseAuth.getInstance().also { authInstance = it }
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseAuth instance unavailable: ${e.message}")
            null
        }

    val storage: FirebaseStorage?
        get() = try {
            storageInstance ?: FirebaseStorage.getInstance().also { storageInstance = it }
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseStorage instance unavailable: ${e.message}")
            null
        }

    /**
     * Initializes Firebase and configures services (Firestore settings, Auth, Storage).
     * Called during application startup in [com.example.HealthVaultApplication.onCreate].
     */
    fun initialize(context: Context) {
        if (isInitialized) {
            Log.d(TAG, "FirebaseInitializer: Already initialized.")
            return
        }

        synchronized(this) {
            if (isInitialized) return

            try {
                val firebaseApp = FirebaseApp.initializeApp(context)
                if (firebaseApp != null) {
                    Log.i(TAG, "FirebaseApp initialized successfully: ${firebaseApp.name}")
                } else {
                    Log.i(TAG, "FirebaseApp auto-initialized by Google Services Provider")
                }
            } catch (e: Throwable) {
                Log.w(TAG, "FirebaseApp initialize note: ${e.message}")
            }

            try {
                val fs = FirebaseFirestore.getInstance()
                try {
                    fs.firestoreSettings = FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
                } catch (e: Throwable) {
                    Log.w(TAG, "Firestore settings persistence note: ${e.message}")
                }
                firestoreInstance = fs
            } catch (e: Throwable) {
                Log.w(TAG, "Firestore initialization note: ${e.message}")
            }

            try {
                authInstance = FirebaseAuth.getInstance()
            } catch (e: Throwable) {
                Log.w(TAG, "FirebaseAuth initialization note: ${e.message}")
            }

            try {
                storageInstance = FirebaseStorage.getInstance()
            } catch (e: Throwable) {
                Log.w(TAG, "FirebaseStorage initialization note: ${e.message}")
            }

            isInitialized = true
            Log.i(TAG, "Firebase services initialization cycle completed safely.")
        }
    }
}
