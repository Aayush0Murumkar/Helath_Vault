package com.example.ai.summarizer

import com.example.data.entity.EncryptedRecordEntity

interface AiMedicalSummarizer {
    enum class Provider {
        MOCK_AI_ARCHITECT,
        GEMINI_PRO_MED,
        FIREBASE_VERTEX_AI
    }

    val provider: Provider

    suspend fun generateSummary(
        record: EncryptedRecordEntity,
        decryptedText: String
    ): AiSummaryResult
}

data class AiSummaryResult(
    val summaryText: String,
    val detectedDiagnosis: String = "",
    val detectedMedicines: String = ""
)

class MockAiSummarizer : AiMedicalSummarizer {
    override val provider = AiMedicalSummarizer.Provider.MOCK_AI_ARCHITECT

    override suspend fun generateSummary(
        record: EncryptedRecordEntity,
        decryptedText: String
    ): AiSummaryResult {
        val placeholder = "[AI Clinical Summary Placeholder]\nRecord '${record.getDisplayTitle()}' categorized as ${record.fileCategory}. Architecture ready for Gemini 1.5 Pro / Vertex AI automated clinical summarization."
        return AiSummaryResult(
            summaryText = placeholder,
            detectedDiagnosis = record.diagnosis.ifBlank { "Type 1 Diabetes, Asthma" },
            detectedMedicines = record.medicines.ifBlank { "Lantus Solostar, Ventolin HFA" }
        )
    }
}

object AiSummarizerFactory {
    private var activeSummarizer: AiMedicalSummarizer = MockAiSummarizer()

    fun getActiveSummarizer(): AiMedicalSummarizer = activeSummarizer

    fun registerSummarizer(summarizer: AiMedicalSummarizer) {
        activeSummarizer = summarizer
    }
}
