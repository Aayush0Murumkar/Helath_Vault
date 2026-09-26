package com.example.backend.auth

import kotlinx.coroutines.flow.Flow

/**
 * Data model representing an authenticated user session in Health Vault.
 */
data class UserSession(
    val userId: String,
    val email: String,
    val displayName: String,
    val userRole: String = "PATIENT",
    val isEmailVerified: Boolean = true,
    val authToken: String? = null,
    val photoUrl: String? = null,
    val lastLoginTimestamp: Long = System.currentTimeMillis()
)

/**
 * Clean Interface for Authentication Services.
 * Ready for Firebase Authentication or Custom OAuth Providers.
 */
interface IAuthRepository {
    fun getAuthStateFlow(): Flow<UserSession?>
    suspend fun getCurrentUser(): UserSession?
    suspend fun signInWithEmail(email: String, pass: String): Result<UserSession>
    suspend fun signUpWithEmail(email: String, pass: String, displayName: String, role: String = "PATIENT"): Result<UserSession>
    suspend fun signOut(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Boolean>
    suspend fun updateProfile(displayName: String, photoUrl: String? = null): Result<UserSession>
}
