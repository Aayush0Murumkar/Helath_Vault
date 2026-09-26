package com.example.backend.storage

import android.content.Context
import java.io.File

/**
 * Local file system implementation of [ICloudStorageService].
 * Stores zero-knowledge encrypted blobs inside application app-private files directory.
 */
class LocalStorageService(private val context: Context) : ICloudStorageService {

    private val vaultDir: File by lazy {
        File(context.filesDir, "encrypted_health_vault").apply { if (!exists()) mkdirs() }
    }

    override suspend fun uploadEncryptedBlob(fileId: String, ciphertextBase64: String): Result<String> {
        return try {
            val targetFile = File(vaultDir, "$fileId.enc")
            targetFile.writeText(ciphertextBase64)
            Result.success("file://${targetFile.absolutePath}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadEncryptedBlob(fileId: String): Result<String> {
        return try {
            val targetFile = File(vaultDir, "$fileId.enc")
            if (targetFile.exists()) {
                Result.success(targetFile.readText())
            } else {
                Result.failure(Exception("Encrypted blob $fileId not found locally"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCloudBlob(fileId: String): Result<Boolean> {
        val targetFile = File(vaultDir, "$fileId.enc")
        if (targetFile.exists()) {
            targetFile.delete()
        }
        return Result.success(true)
    }

    override suspend fun getStorageUsageBytes(): Long {
        return vaultDir.listFiles()?.sumOf { it.length() } ?: 0L
    }
}
