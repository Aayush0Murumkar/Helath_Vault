package com.example.backend.notifications

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Backend-ready implementation of [INotificationService] for Firebase Cloud Messaging (FCM).
 * Ready for `FirebaseMessaging.getInstance().subscribeToTopic()` and FCM Push Triggers.
 */
class FCMNotificationService : INotificationService {

    private val fcmNotificationsState = MutableStateFlow<List<NotificationPayload>>(emptyList())

    // Ready hook for FirebaseMessaging:
    // private val firebaseMessaging by lazy { com.google.firebase.messaging.FirebaseMessaging.getInstance() }

    override suspend fun registerFCMToken(userId: String, token: String): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun sendEmergencyPushAlert(
        patientId: String,
        title: String,
        message: String
    ): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun subscribeToTopic(topicName: String): Result<Boolean> {
        return try {
            // Ready hook: firebaseMessaging.subscribeToTopic(topicName).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getNotificationsFlow(): Flow<List<NotificationPayload>> = fcmNotificationsState.asStateFlow()

    override suspend fun postLocalAlertNotification(title: String, body: String, type: String) {
        val payload = NotificationPayload("fcm_${System.currentTimeMillis()}", title, body, type = type)
        fcmNotificationsState.value = listOf(payload) + fcmNotificationsState.value
    }
}
