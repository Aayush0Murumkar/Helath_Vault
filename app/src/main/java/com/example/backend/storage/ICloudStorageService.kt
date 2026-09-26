package com.example.backend.storage

/**
 * Interface for Cloud File Storage (Encrypted Document Blobs).
 * Bridges local sandbox storage and Firebase Storage / Cloud Buckets.
 */
interface ICloudStorageService {
    suspend fun uploadEncryptedBlob(fileId: String, ciphertextBase64: String): Result<String>
    suspend fun downloadEncryptedBlob(fileId: String): Result<String>
    suspend fun deleteCloudBlob(fileId: String): Result<Boolean>
    suspend fun getStorageUsageBytes(): Long
}
