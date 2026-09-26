package com.example.backend.notifications

import kotlinx.coroutines.flow.Flow

data class NotificationPayload(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "GENERAL_ALERT",
    val isRead: Boolean = false
)

/**
 * Interface for Push Notifications & Emergency Alerts.
 * Bridges local Android NotificationManager and Firebase Cloud Messaging (FCM).
 */
interface INotificationService {
    suspend fun registerFCMToken(userId: String, token: String): Result<Boolean>
    suspend fun sendEmergencyPushAlert(patientId: String, title: String, message: String): Result<Boolean>
    suspend fun subscribeToTopic(topicName: String): Result<Boolean>
    fun getNotificationsFlow(): Flow<List<NotificationPayload>>
    suspend fun postLocalAlertNotification(title: String, body: String, type: String = "ALERT")
}
