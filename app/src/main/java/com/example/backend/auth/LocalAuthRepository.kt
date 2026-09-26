package com.example.backend.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Local offline-first implementation of [IAuthRepository].
 * Simulates a logged-in user session using local Health Vault state.
 */
class LocalAuthRepository : IAuthRepository {

    private val currentUserSession = MutableStateFlow<UserSession?>(
        UserSession(
            userId = "PAT-98421",
            email = "sarah.jenkins@healthvault.org",
            displayName = "Sarah Jenkins",
            userRole = "PATIENT",
            isEmailVerified = true,
            authToken = "LOCAL_ZK_SESSION_TOKEN_98421"
        )
    )

    override fun getAuthStateFlow(): Flow<UserSession?> = currentUserSession.asStateFlow()

    override suspend fun getCurrentUser(): UserSession? = currentUserSession.value

    override suspend fun signInWithEmail(email: String, pass: String): Result<UserSession> {
        val session = UserSession(
            userId = "PAT-98421",
            email = email,
            displayName = email.substringBefore("@").replace(".", " ").capitalize(),
            userRole = "PATIENT",
            isEmailVerified = true,
            authToken = "LOCAL_ZK_SESSION_TOKEN_" + System.currentTimeMillis()
        )
        currentUserSession.value = session
        return Result.success(session)
    }

    override suspend fun signUpWithEmail(
        email: String,
        pass: String,
        displayName: String,
        role: String
    ): Result<UserSession> {
        val session = UserSession(
            userId = "PAT-" + System.currentTimeMillis().toString().takeLast(5),
            email = email,
            displayName = displayName,
            userRole = role,
            isEmailVerified = true,
            authToken = "LOCAL_ZK_SESSION_TOKEN_" + System.currentTimeMillis()
        )
        currentUserSession.value = session
        return Result.success(session)
    }

    override suspend fun signOut(): Result<Unit> {
        currentUserSession.value = null
        return Result.success(Unit)
    }

    override suspend fun resetPassword(email: String): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun updateProfile(displayName: String, photoUrl: String?): Result<UserSession> {
        val current = currentUserSession.value ?: return Result.failure(Exception("No active user session"))
        val updated = current.copy(displayName = displayName, photoUrl = photoUrl ?: current.photoUrl)
        currentUserSession.value = updated
        return Result.success(updated)
    }
}
