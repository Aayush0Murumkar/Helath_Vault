package com.example.backend.functions

/**
 * Interface for Firebase Cloud Functions / Serverless Microservices.
 * Used for license validation, emergency SMS triggers, temporary zero-trust token generation,
 * and server-side encrypted processing.
 */
interface ICloudFunctionsService {
    suspend fun verifyDoctorLicenseWithMedicalRegistry(doctorId: String, licenseNumber: String): Result<Boolean>
    suspend fun triggerEmergencyAccessWebhook(patientId: String, doctorName: String, reason: String): Result<Boolean>
    suspend fun generateTemporaryAccessToken(requestId: String, durationMinutes: Int): Result<String>
    suspend fun runServerSideGeminiAnalysis(documentPayload: String): Result<String>
}
