package com.example.backend.notifications

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Local implementation of [INotificationService].
 */
class LocalNotificationService(private val context: Context) : INotificationService {

    private val notificationsState = MutableStateFlow(
        listOf(
            NotificationPayload(
                id = "n_1",
                title = "🔒 Security Vault Lock Active",
                body = "Zero-Knowledge local encryption key verified.",
                timestamp = System.currentTimeMillis() - 3600000,
                type = "SECURITY"
            ),
            NotificationPayload(
                id = "n_2",
                title = "🚨 Emergency Access Permission",
                body = "Dr. Marcus Vance requested 30-min record access.",
                timestamp = System.currentTimeMillis() - 1800000,
                type = "EMERGENCY"
            )
        )
    )

    override suspend fun registerFCMToken(userId: String, token: String): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun sendEmergencyPushAlert(
        patientId: String,
        title: String,
        message: String
    ): Result<Boolean> {
        postLocalAlertNotification(title, message, "EMERGENCY_PUSH")
        return Result.success(true)
    }

    override suspend fun subscribeToTopic(topicName: String): Result<Boolean> {
        return Result.success(true)
    }

    override fun getNotificationsFlow(): Flow<List<NotificationPayload>> = notificationsState.asStateFlow()

    override suspend fun postLocalAlertNotification(title: String, body: String, type: String) {
        val newNotif = NotificationPayload(
            id = "n_${System.currentTimeMillis()}",
            title = title,
            body = body,
            timestamp = System.currentTimeMillis(),
            type = type
        )
        notificationsState.value = listOf(newNotif) + notificationsState.value
    }
}
