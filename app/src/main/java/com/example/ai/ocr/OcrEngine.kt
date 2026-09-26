package com.example.ai.ocr

interface OcrEngine {
    enum class Provider {
        MOCK_LOCAL_EXTRACTOR,
        GOOGLE_VISION_API,
        GEMINI_VISION_API
    }

    val provider: Provider

    suspend fun extractText(
        filePayloadBase64: String,
        originalFileName: String,
        fileCategory: String
    ): OcrExtractionResult
}

data class OcrExtractionResult(
    val ocrText: String,
    val confidenceScore: Float = 0.98f,
    val extractedFields: Map<String, String> = emptyMap()
)

class MockOcrEngine : OcrEngine {
    override val provider = OcrEngine.Provider.MOCK_LOCAL_EXTRACTOR

    override suspend fun extractText(
        filePayloadBase64: String,
        originalFileName: String,
        fileCategory: String
    ): OcrExtractionResult {
        val ocrPlaceholder = "[OCR Extracted Text - Pending Vision API]\nDocument: $originalFileName ($fileCategory)\nStatus: Automated text extraction placeholder ready for Google Vision API / Gemini integration.\nKey Data: Patient Sarah Jenkins (PAT-98421). Normal baseline clinical parameters recorded."
        return OcrExtractionResult(
            ocrText = ocrPlaceholder,
            confidenceScore = 0.95f,
            extractedFields = mapOf("fileName" to originalFileName, "status" to "PROCESSED")
        )
    }
}

object OcrProcessorFactory {
    private var activeEngine: OcrEngine = MockOcrEngine()

    fun getActiveEngine(): OcrEngine = activeEngine

    fun registerEngine(engine: OcrEngine) {
        activeEngine = engine
    }
}
