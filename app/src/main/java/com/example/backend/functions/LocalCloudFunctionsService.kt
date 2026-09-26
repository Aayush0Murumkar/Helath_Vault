package com.example.backend.functions

/**
 * Local simulation implementation of [ICloudFunctionsService].
 */
class LocalCloudFunctionsService : ICloudFunctionsService {

    override suspend fun verifyDoctorLicenseWithMedicalRegistry(
        doctorId: String,
        licenseNumber: String
    ): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun triggerEmergencyAccessWebhook(
        patientId: String,
        doctorName: String,
        reason: String
    ): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun generateTemporaryAccessToken(
        requestId: String,
        durationMinutes: Int
    ): Result<String> {
        val token = "LOCAL_ZK_ACCESS_TOKEN_${requestId}_$durationMinutes"
        return Result.success(token)
    }

    override suspend fun runServerSideGeminiAnalysis(documentPayload: String): Result<String> {
        return Result.success("Server-side Gemini analysis completed successfully.")
    }
}
