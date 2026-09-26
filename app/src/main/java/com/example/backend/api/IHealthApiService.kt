package com.example.backend.api

/**
 * Interface for External Health APIs, Gemini Intelligence REST endpoints, and Medical OCR/Summarization Services.
 */
interface IHealthApiService {
    suspend fun queryGeminiHealthAssistant(prompt: String, contextHistory: List<String> = emptyList()): Result<String>
    suspend fun fetchMedicineInteractions(medicineNames: List<String>): Result<String>
    suspend fun parseMedicalReportOCR(documentBase64: String): Result<String>
}
